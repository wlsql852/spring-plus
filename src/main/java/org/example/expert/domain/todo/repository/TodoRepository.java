package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TodoRepository extends JpaRepository<Todo, Long> , TodoDslRepository {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("SELECT t FROM Todo t WHERE t.weather LIKE :weather order by t.modifiedAt desc")
    Page<Todo> findAllByWeather(Pageable pageable, String weather);

    @Query("SELECT t FROM Todo t WHERE t.modifiedAt >= date(:startDate) order by t.modifiedAt desc")
    Page<Todo> findAllByModifiedAtAfter(Pageable pageable, String startDate);

    @Query("SELECT t FROM Todo t WHERE t.modifiedAt <= date (:endDate) order by t.modifiedAt desc")
    Page<Todo> findAllByModifiedAtBefore(Pageable pageable, String endDate);

    @Query("SELECT t FROM Todo t WHERE t.weather LIKE :weather AND t.modifiedAt >= date(:startDate) order by t.modifiedAt desc")
    Page<Todo> findAllByWeatherAndModifiedAtAfter(Pageable pageable, String weather, String startDate);

    @Query("SELECT t FROM Todo t WHERE t.weather LIKE :weather AND t.modifiedAt <= date(:endDate) order by t.modifiedAt desc")
    Page<Todo> findAllByWeatherAndModifiedAtBefore(Pageable pageable, String weather, String endDate);

    @Query("SELECT t FROM Todo t WHERE t.modifiedAt between date(:startDate) And date(:endDate) order by t.modifiedAt desc")
    Page<Todo> findAllByModifiedAtBetween(Pageable pageable, String startDate, String endDate);

    @Query("SELECT t FROM Todo t WHERE t.weather LIKE :weather AND (t.modifiedAt between date(:startDate) And date(:endDate)) order by t.modifiedAt desc")
    Page<Todo> findAllByWeatherAndModifiedAtBetween(Pageable pageable, String weather, String startDate, String endDate);




//    @Query("SELECT t FROM Todo t " +
//            "LEFT JOIN t.user " +
//            "WHERE t.id = :todoId")
//    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
