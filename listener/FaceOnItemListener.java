package com.lingtuan.firefly.listener;

/**
 * 表情点击监听类
 */
public interface FaceOnItemListener {

	/**
	 * 表情选择
	 * @param page 第几页
	 * @param position 第几个表情
	 * @param isDelete 删除按钮
	 * @param  isGif 是否是gif图
	 */
	void onItemListener(int page, int position, boolean isGif, boolean isDelete);
	
}
