package cn.lw.fz.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {

    @Transactional
    @Modifying
    @Query(value = "insert into user_del select * from user where id = ?1", nativeQuery = true)
    int bak(int id);
}
