package cn.lw.fz.db;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table
@Entity
@Data
public class User {
    @Id
    private int id;
    @Column(name = "email")
    private String email;
    @Column(name = "url")
    private String url;
    @Column(name = "rest")
    private String rest;
    @Column(name = "create_time")
    private Date createTime;
}
