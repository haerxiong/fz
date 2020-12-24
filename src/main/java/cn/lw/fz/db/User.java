package cn.lw.fz.db;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

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
    @Transient
    private String ips;
}
