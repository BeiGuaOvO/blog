/** 在文档树中定位节点，返回从根到该节点的路径 */
export function findPath(nodes, id, trail = []) {
  for (const node of nodes || []) {
    const next = [...trail, node]
    if (node.id === id) return next
    if (node.children?.length) {
      const found = findPath(node.children, id, next)
      if (found) return found
    }
  }
  return null
}

/** 扁平化，用于构造下拉选项 */
export function flatten(nodes, depth = 0, out = []) {
  for (const node of nodes || []) {
    out.push({ ...node, depth })
    if (node.children?.length) flatten(node.children, depth + 1, out)
  }
  return out
}

/** 排除某个节点及其所有子孙（防止父节点选到自己或后代） */
export function excludeSubtree(nodes, id) {
  return (nodes || [])
    .filter((n) => n.id !== id)
    .map((n) => ({ ...n, children: excludeSubtree(n.children, id) }))
}