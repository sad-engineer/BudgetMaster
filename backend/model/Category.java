// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Category extends BaseEntity {
    private int position;
    private String title;
    private int operationType;
    private int type;
    private Integer parentId;

    public Category() {}

    public Category(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                    String createdBy, String updatedBy, String deletedBy, int position, String title, int operationType, int type, Integer parentId) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.position = position;
        this.title = title;
        this.operationType = operationType;
        this.type = type;
        this.parentId = parentId;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getOperationType() { return operationType; }
    public void setOperationType(int operationType) { this.operationType = operationType; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        if (!super.equals(o)) return false;
        Category category = (Category) o;
        return position == category.position && operationType == category.operationType && type == category.type &&
                Objects.equals(title, category.title) &&
                Objects.equals(parentId, category.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title, operationType, type, parentId);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                ", operationType=" + operationType +
                ", type=" + type +
                ", parentId=" + parentId +
                '}';
    }
} 