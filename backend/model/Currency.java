// -*- coding: utf-8 -*-
package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Currency extends BaseEntity {
    private int position;
    private String title;

    public Currency() {}

    public Currency(int id, LocalDateTime createTime, LocalDateTime updateTime, LocalDateTime deleteTime,
                    String createdBy, String updatedBy, String deletedBy, int position, String title) {
        super(id, createTime, updateTime, deleteTime, createdBy, updatedBy, deletedBy);
        this.position = position;
        this.title = title;
    }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        if (!super.equals(o)) return false;
        Currency currency = (Currency) o;
        return position == currency.position && Objects.equals(title, currency.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position, title);
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                '}';
    }
} 