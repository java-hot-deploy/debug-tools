/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.base.hutool.core.lang.tree;

import io.github.future0923.debug.tools.base.hutool.core.builder.Builder;
import io.github.future0923.debug.tools.base.hutool.core.collection.CollUtil;
import io.github.future0923.debug.tools.base.hutool.core.lang.Assert;
import io.github.future0923.debug.tools.base.hutool.core.lang.tree.parser.NodeParser;
import io.github.future0923.debug.tools.base.hutool.core.map.MapUtil;
import io.github.future0923.debug.tools.base.hutool.core.util.ObjectUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 树构建器
 *
 * @param <E> ID类型
 */
public class TreeBuilder<E> implements Builder<Tree<E>> {
	private static final long serialVersionUID = 1L;

	private Tree<E> root;
	private final Map<E, Tree<E>> idTreeMap;
	private boolean isBuild;

	/**
	 * 创建Tree构建器
	 *
	 * @param rootId 根节点ID
	 * @param <T>    ID类型
	 * @return TreeBuilder
	 */
	public static <T> TreeBuilder<T> of(T rootId) {
		return of(rootId, null);
	}

	/**
	 * 创建Tree构建器
	 *
	 * @param rootId 根节点ID
	 * @param config 配置
	 * @param <T>    ID类型
	 * @return TreeBuilder
	 */
	public static <T> TreeBuilder<T> of(T rootId, TreeNodeConfig config) {
		return new TreeBuilder<>(rootId, config);
	}

	/**
	 * 构造
	 *
	 * @param rootId 根节点ID
	 * @param config 配置
	 */
	public TreeBuilder(E rootId, TreeNodeConfig config) {
		this(new Tree<E>(config).setId(rootId));
	}

	/**
	 * 构造
	 *
	 * @param rootNode 根节点
	 * @since 5.8.33
	 */
	public TreeBuilder(Tree<E> rootNode) {
		this.root = rootNode;
		this.idTreeMap = new LinkedHashMap<>();
	}

	/**
	 * 设置ID
	 *
	 * @param id ID
	 * @return this
	 * @since 5.7.14
	 */
	public TreeBuilder<E> setId(E id) {
		this.root.setId(id);
		return this;
	}

	/**
	 * 设置父节点ID
	 *
	 * @param parentId 父节点ID
	 * @return this
	 * @since 5.7.14
	 */
	public TreeBuilder<E> setParentId(E parentId) {
		this.root.setParentId(parentId);
		return this;
	}

	/**
	 * 设置节点标签名称
	 *
	 * @param name 节点标签名称
	 * @return this
	 * @since 5.7.14
	 */
	public TreeBuilder<E> setName(CharSequence name) {
		this.root.setName(name);
		return this;
	}

	/**
	 * 设置权重
	 *
	 * @param weight 权重
	 * @return this
	 * @since 5.7.14
	 */
	public TreeBuilder<E> setWeight(Comparable<?> weight) {
		this.root.setWeight(weight);
		return this;
	}

	/**
	 * 扩展属性
	 *
	 * @param key   键
	 * @param value 扩展值
	 * @return this
	 * @since 5.7.14
	 */
	public TreeBuilder<E> putExtra(String key, Object value) {
		Assert.notEmpty(key, "Key must be not empty !");
		this.root.put(key, value);
		return this;
	}

	/**
	 * 增加节点列表，增加的节点是不带子节点的
	 *
	 * @param map 节点列表
	 * @return this
	 */
	public TreeBuilder<E> append(Map<E, Tree<E>> map) {
		checkBuilt();

		this.idTreeMap.putAll(map);
		return this;
	}

	/**
	 * 增加节点列表，增加的节点是不带子节点的
	 *
	 * @param trees 节点列表
	 * @return this
	 */
	public TreeBuilder<E> append(Iterable<Tree<E>> trees) {
		checkBuilt();

		for (Tree<E> tree : trees) {
			this.idTreeMap.put(tree.getId(), tree);
		}
		return this;
	}

	/**
	 * 增加节点列表，增加的节点是不带子节点的
	 *
	 * @param list       Bean列表
	 * @param <T>        Bean类型
	 * @param nodeParser 节点转换器，用于定义一个Bean如何转换为Tree节点
	 * @return this
	 */
	public <T> TreeBuilder<E> append(List<T> list, NodeParser<T, E> nodeParser) {
		checkBuilt();

		final TreeNodeConfig config = this.root.getConfig();
		final E rootId = this.root.getId();
		final Map<E, Tree<E>> map = this.idTreeMap;
		Tree<E> node;
		for (T t : list) {
			node = new Tree<>(config);
			nodeParser.parse(t, node);
			if (null != rootId && false == rootId.getClass().equals(node.getId().getClass())) {
				throw new IllegalArgumentException("rootId type is node.getId().getClass()!");
			}
			// issue#IAUSHR 如果指定根节点存在，直接复用
			if(Objects.equals(rootId, node.getId())){
				this.root = node;
			}else {
				//非根节点
				map.put(node.getId(), node);
			}

		}
		return append(map);
	}

	/**
	 * 增加节点列表，增加的节点是不带子节点的
	 *
	 * @param <T>        Bean类型
	 * @param list       Bean列表
	 * @param rootId     根ID
	 * @param nodeParser 节点转换器，用于定义一个Bean如何转换为Tree节点
	 * @return this
	 * @since 5.8.6
	 * @deprecated rootId参数可以不提供，在root节点中直接获取，请使用{@link #append(List, NodeParser)}
	 */
	@Deprecated
	public <T> TreeBuilder<E> append(List<T> list, E rootId, NodeParser<T, E> nodeParser) {
		checkBuilt();

		final TreeNodeConfig config = this.root.getConfig();
		final Map<E, Tree<E>> map = this.idTreeMap;
		Tree<E> node;
		for (T t : list) {
			node = new Tree<>(config);
			nodeParser.parse(t, node);
			if (null != rootId && false == rootId.getClass().equals(node.getId().getClass())) {
				throw new IllegalArgumentException("rootId type is node.getId().getClass()!");
			}
			// issue#IAUSHR 如果指定根节点存在，直接复用
			if(Objects.equals(rootId, node.getId())){
				this.root = node;
			}else {
				//非根节点
				map.put(node.getId(), node);
			}

		}
		return append(map);
	}

	/**
	 * 重置Builder，实现复用
	 *
	 * @return this
	 */
	public TreeBuilder<E> reset() {
		this.idTreeMap.clear();
		this.root.setChildren(null);
		this.isBuild = false;
		return this;
	}

	@Override
	public Tree<E> build() {
		checkBuilt();

		buildFromMap();
		cutTree();

		this.isBuild = true;
		this.idTreeMap.clear();

		return root;
	}

	/**
	 * 构建树列表，没有顶层节点，例如：
	 *
	 * <pre>
	 * -用户管理
	 *  -用户管理
	 *    +用户添加
	 * - 部门管理
	 *  -部门管理
	 *    +部门添加
	 * </pre>
	 *
	 * @return 树列表
	 */
	public List<Tree<E>> buildList() {
		if (isBuild) {
			// 已经构建过了
			return this.root.getChildren();
		}
		return build().getChildren();
	}

	/**
	 * 开始构建
	 */
	private void buildFromMap() {
		if (MapUtil.isEmpty(this.idTreeMap)) {
			return;
		}

		final Map<E, Tree<E>> eTreeMap = MapUtil.sortByValue(this.idTreeMap, false);
		E parentId;
		for (Tree<E> node : eTreeMap.values()) {
			if (null == node) {
				continue;
			}
			parentId = node.getParentId();
			if (ObjectUtil.equals(this.root.getId(), parentId)) {
				this.root.addChildren(node);
				continue;
			}

			final Tree<E> parentNode = eTreeMap.get(parentId);
			if (null != parentNode) {
				parentNode.addChildren(node);
			}
		}
	}

	/**
	 * 树剪枝
	 */
	private void cutTree() {
		final TreeNodeConfig config = this.root.getConfig();
		final Integer deep = config.getDeep();
		if (null == deep || deep < 0) {
			return;
		}
		cutTree(this.root, 0, deep);
	}

	/**
	 * 树剪枝叶
	 *
	 * @param tree        节点
	 * @param currentDepp 当前层级
	 * @param maxDeep     最大层级
	 */
	private void cutTree(Tree<E> tree, int currentDepp, int maxDeep) {
		if (null == tree) {
			return;
		}
		if (currentDepp == maxDeep) {
			// 剪枝
			tree.setChildren(null);
			return;
		}

		final List<Tree<E>> children = tree.getChildren();
		if (CollUtil.isNotEmpty(children)) {
			for (Tree<E> child : children) {
				cutTree(child, currentDepp + 1, maxDeep);
			}
		}
	}

	/**
	 * 检查是否已经构建
	 */
	private void checkBuilt() {
		Assert.isFalse(isBuild, "Current tree has been built.");
	}
}
