package org.likelion.hsu.ssak3.repository;

import org.likelion.hsu.ssak3.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    UserProfile findByKakaoId(Long kakaoId);
}
