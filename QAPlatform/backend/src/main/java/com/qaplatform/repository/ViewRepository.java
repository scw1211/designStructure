package com.qaplatform.repository;

import com.qaplatform.dto.ExpertUserDTO;
import com.qaplatform.dto.TrendingQuestionDTO;
import com.qaplatform.dto.UserStatisticsViewDTO;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;

/**
 * 数据库视图查询Repository
 */
@Repository
public class ViewRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 查询用户统计视图
     */
    public Optional<Object[]> findUserStatistics(Long userId) {
        String sql = "SELECT " +
                "user_id, username, email, reputation, role, user_created_at, " +
                "total_questions, total_answers, accepted_answers, total_votes_received, " +
                "total_votes_given, questions_followed, users_following, followers_count, " +
                "badges_count, acceptance_rate, average_answer_score, last_active_date, " +
                "activity_count_7d, activity_count_30d " +
                "FROM v_user_statistics WHERE user_id = ?1";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, userId);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 获取所有用户统计信息
     */
    public List<Object[]> findAllUserStatistics(int limit, int offset) {
        String sql = "SELECT " +
                "user_id, username, email, reputation, role, user_created_at, " +
                "total_questions, total_answers, accepted_answers, total_votes_received, " +
                "total_votes_given, questions_followed, users_following, followers_count, " +
                "badges_count, acceptance_rate, average_answer_score, last_active_date, " +
                "activity_count_7d, activity_count_30d " +
                "FROM v_user_statistics ORDER BY total_votes_received DESC " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询热门问题
     */
    public List<Object[]> findTrendingQuestions(int limit, int offset) {
        String sql = "SELECT " +
                "id, title, content, view_count, answer_count, votes, solved, " +
                "created_at, updated_at, author_username, author_reputation, " +
                "trending_score, tags " +
                "FROM v_trending_questions " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询专家用户
     */
    public List<Object[]> findExpertUsers(int limit, int offset) {
        String sql = "SELECT " +
                "id, username, reputation, avatar, total_questions, total_answers, " +
                "accepted_answers, acceptance_rate, total_votes_received, badges_count, " +
                "average_answer_score, expertise_score, top_tags " +
                "FROM v_expert_users " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询标签统计
     */
    public List<Object[]> findTagStatistics(int limit, int offset) {
        String sql = "SELECT " +
                "id, name, description, usage_count, question_count, answer_count, " +
                "unique_askers, unique_answerers, avg_question_votes, avg_answer_votes, " +
                "solved_questions, solve_rate, last_question_date " +
                "FROM v_tag_statistics ORDER BY usage_count DESC " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询活跃度统计
     */
    public List<Object[]> findActivityStatistics(int limit, int offset) {
        String sql = "SELECT " +
                "activity_date, total_activities, active_users, questions_asked, " +
                "answers_given, votes_cast, questions_viewed, logins " +
                "FROM v_activity_statistics ORDER BY activity_date DESC " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询用户活跃度排行榜
     */
    public List<Object[]> findUserActivityLeaderboard(int limit, int offset) {
        String sql = "SELECT " +
                "user_id, username, daily_score, weekly_score, monthly_score, " +
                "total_score, last_active, days_streak " +
                "FROM v_user_activity_leaderboard ORDER BY total_score DESC " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
    
    /**
     * 查询问题质量评分
     */
    public List<Object[]> findQuestionQualityScores(int limit, int offset) {
        String sql = "SELECT " +
                "question_id, title, author_username, quality_score, vote_ratio, " +
                "answer_quality, view_engagement, tag_relevance " +
                "FROM v_question_quality_scores ORDER BY quality_score DESC " +
                "LIMIT ?1 OFFSET ?2";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, limit);
        query.setParameter(2, offset);
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
} 