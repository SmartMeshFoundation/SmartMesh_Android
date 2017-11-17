package com.lingtuan.firefly.contact.vo;

import java.io.Serializable;

public class GroupImageVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String pic;
    private boolean isNew;//The picture of the new

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {

        this.pic = pic;
    }
}
