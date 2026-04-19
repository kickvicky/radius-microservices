package com.radius.feed.repository;

import com.radius.feed.entity.Post;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query(value = "SELECT * FROM posts p WHERE ST_DWithin(p.location, :point, :radiusMeters)",
            nativeQuery = true)
    List<Post> findPostsWithinRadius(@Param("point") Point point,
                                    @Param("radiusMeters") double radiusMeters);

}
