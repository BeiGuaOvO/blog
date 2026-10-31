import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'
import markdownItKatex from '@vscode/markdown-it-katex'

const slugify = (text) =>
  String(text)
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '') || 'section'

const md = new MarkdownIt({
  html: false,        // 禁止原始 HTML，从源头挡住大部分 XSS
  linkify: true,
  breaks: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        const out = hljs.highlight(code, { language: lang, ignoreIllegals: true }).value
        return `<pre class="hljs"><code class="language-${lang}">${out}</code></pre>`
      } catch (_) { /* fallthrough */ }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(code)}</code></pre>`
  }
})

// ★ 数学公式：行内 $...$，块级 $$...$$
md.use(markdownItKatex, {
  throwOnError: false,   // 语法错误时显示红色原文，而不是抛异常让整篇白屏
  errorColor: '#e74c3c',
  output: 'html'
})

// 外链新窗口打开
const defaultLinkOpen =
  md.renderer.rules.link_open ||
  ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  tokens[idx].attrSet('target', '_blank')
  tokens[idx].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen(tokens, idx, options, env, self)
}

// 标题加 id + 收集目录
md.core.ruler.push('toc_anchor', (state) => {
  const env = state.env
  if (!env || !Array.isArray(env.toc)) return
  env.toc.length = 0
  const counter = new Map()
  const tokens = state.tokens
  for (let i = 0; i < tokens.length; i++) {
    const token = tokens[i]
    if (token.type !== 'heading_open') continue
    const text = (tokens[i + 1]?.content || '').trim()
    if (!text) continue
    let id = slugify(text)
    const n = (counter.get(id) || 0) + 1
    counter.set(id, n)
    if (n > 1) id = `${id}-${n}`
    token.attrSet('id', id)
    env.toc.push({ level: Number(token.tag.slice(1)), text, id })
  }
})

export function renderMarkdown(source) {
  const env = { toc: [] }
  const html = md.render(source || '', env)
  // DOMPurify 默认放行 MathML 标签，KaTeX 的 <math>/<span class="katex"> 不会被删
  return { html: DOMPurify.sanitize(html), toc: env.toc }
}

/** 纯文本摘要，用于没有 summary 的文章卡片 */
export function plainText(source, max = 140) {
  const text = String(source || '')
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/\$\$[\s\S]*?\$\$/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return text.length > max ? `${text.slice(0, max)}…` : text
}