package com.github.beiguaovo.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.beiguaovo.blog.common.BizException;
import com.github.beiguaovo.blog.dto.DocDTO;
import com.github.beiguaovo.blog.entity.Doc;
import com.github.beiguaovo.blog.mapper.DocMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocService {

    private final DocMapper docMapper;

    /** 一次查出全部节点在内存组树，几千级文档完全够用 */
    public List<Doc> tree(boolean onlyPublic) {
        LambdaQueryWrapper<Doc> w = new LambdaQueryWrapper<Doc>()
                .select(Doc::getId, Doc::getTitle, Doc::getSlug, Doc::getParentId,
                        Doc::getSort, Doc::getIsPublic)
                .orderByAsc(Doc::getSort).orderByAsc(Doc::getId);
        if (onlyPublic) {
            w.eq(Doc::getIsPublic, 1);
        }
        List<Doc> docs = docMapper.selectList(w);

        Map<Long, Doc> map = new LinkedHashMap<>();
        docs.forEach(d -> map.put(d.getId(), d));
        List<Doc> roots = new ArrayList<>();
        for (Doc doc : docs) {
            Doc parent = doc.getParentId() == null ? null : map.get(doc.getParentId());
            if (parent == null) {
                roots.add(doc);
            } else {
                parent.getChildren().add(doc);
            }
        }
        return roots;
    }

    public Doc detailById(Long id) {
        Doc doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BizException(404, "文档不存在");
        }
        return doc;
    }

    public Doc detailBySlug(String slug) {
        Doc doc = docMapper.selectOne(new LambdaQueryWrapper<Doc>()
                .eq(Doc::getSlug, slug).eq(Doc::getIsPublic, 1));
        if (doc == null) {
            throw new BizException(404, "文档不存在或未公开");
        }
        docMapper.update(null, new LambdaUpdateWrapper<Doc>()
                .eq(Doc::getId, doc.getId())
                .setSql("views = views + 1"));
        doc.setViews((doc.getViews() == null ? 0 : doc.getViews()) + 1);
        return doc;
    }

    public Long create(DocDTO dto) {
        Doc doc = new Doc();
        BeanUtils.copyProperties(dto, doc);
        doc.setId(null);
        doc.setSlug(resolveSlug(dto.getSlug(), dto.getTitle(), null));
        doc.setSort(dto.getSort() == null ? 0 : dto.getSort());
        doc.setIsPublic(dto.getIsPublic() == null ? 1 : dto.getIsPublic());
        doc.setParentId(validParent(dto.getParentId(), null));
        docMapper.insert(doc);
        return doc.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DocDTO dto) {
        Doc old = docMapper.selectById(id);
        if (old == null) {
            throw new BizException(404, "文档不存在");
        }
        Long parentId = validParent(dto.getParentId(), id);

        Doc doc = new Doc();
        BeanUtils.copyProperties(dto, doc);
        doc.setId(id);
        doc.setSlug(resolveSlug(dto.getSlug(), dto.getTitle(), id));
        doc.setSort(dto.getSort() == null ? old.getSort() : dto.getSort());
        doc.setIsPublic(dto.getIsPublic() == null ? old.getIsPublic() : dto.getIsPublic());
        doc.setParentId(parentId);
        docMapper.updateById(doc);

        // parentId 允许置空（移动到根），updateById 会跳过 null，这里显式写一次
        docMapper.update(null, new LambdaUpdateWrapper<Doc>()
                .eq(Doc::getId, id)
                .set(Doc::getParentId, parentId));
    }

    public void delete(Long id) {
        long children = docMapper.selectCount(new LambdaQueryWrapper<Doc>()
                .eq(Doc::getParentId, id));
        if (children > 0) {
            throw new BizException("请先删除子文档");
        }
        docMapper.deleteById(id);
    }

    /** 校验父节点：不能是自己、必须存在 */
    private Long validParent(Long parentId, Long selfId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(selfId)) {
            throw new BizException("父文档不能是自己");
        }
        if (docMapper.selectById(parentId) == null) {
            throw new BizException("父文档不存在");
        }
        return parentId;
    }

    private String resolveSlug(String slug, String title, Long excludeId) {
        String base = StringUtils.hasText(slug) ? slug.trim().toLowerCase()
                : (StringUtils.hasText(title) ? title.trim() : "doc");
        String candidate = base;
        int suffix = 2;
        while (slugExists(candidate, excludeId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean slugExists(String slug, Long excludeId) {
        LambdaQueryWrapper<Doc> w = new LambdaQueryWrapper<Doc>().eq(Doc::getSlug, slug);
        if (excludeId != null) {
            w.ne(Doc::getId, excludeId);
        }
        return docMapper.selectCount(w) > 0;
    }
}