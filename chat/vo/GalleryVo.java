package com.lingtuan.firefly.chat.vo;

import java.io.Serializable;


/**
 * Photo album each photo attribute class
 */
public class GalleryVo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The path of the image in the sd card  */
	public String sdcardPath;
	
	
	/** Pictures in the path of the thumbnail on the sd card */
	public String sdThumbnail;
	
	
	/** Whether the user has selected */
	public boolean isSeleted = false; 
	
	
	/** Real compressed image path, users need to upload the image path */
	public String uploadPath;
	
	/** Whether the camera */
	public boolean isTakePhoto = false ; 
	

}
