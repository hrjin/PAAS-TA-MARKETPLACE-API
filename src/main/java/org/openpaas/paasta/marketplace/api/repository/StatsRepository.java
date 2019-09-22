package org.openpaas.paasta.marketplace.api.repository;

import org.openpaas.paasta.marketplace.api.domain.Instance;
import org.openpaas.paasta.marketplace.api.domain.Software;
import org.openpaas.paasta.marketplace.api.domain.Stats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats<Long, Long>, Long> {

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.status = :status "
            + "AND i.software.id IN :idIn " + "GROUP BY i.software.id")
    List<Object[]> countsOfInsts(@Param("status") Instance.Status status, @Param("idIn") List<Long> idIn);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.createdBy = :providerId AND i.status = :status "
            + "AND i.software.id IN :idIn " + "GROUP BY i.software.id")
    List<Object[]> countsOfInsts(@Param("providerId") String providerId,
            @Param("status") Instance.Status status, @Param("idIn") List<Long> idIn);

    @Query("SELECT i.software.createdBy, COUNT(*) FROM Instance i WHERE i.status = :status "
            + "AND i.software.createdBy IN :idIn " + "GROUP BY i.software.createdBy")
    List<Object[]> countsOfInstsByProviderIds(@Param("status") Instance.Status status,
            @Param("idIn") List<String> idIn);

    @Query("SELECT i.createdBy, COUNT(*) FROM Instance i WHERE i.status = :status "
            + "AND i.createdBy IN :idIn " + "GROUP BY i.createdBy")
    List<Object[]> countsOfInstsByUserIds(@Param("status") Instance.Status status,
            @Param("idIn") List<String> idIn);

    @Query("SELECT COUNT(*) FROM Software s WHERE s.status = :status")
    long countOfSws(@Param("status") Software.Status status);

    @Query("SELECT COUNT(*) FROM Software s WHERE s.createdBy = :providerId AND s.status = :status")
    long countOfSws(@Param("providerId") String providerId, @Param("status") Software.Status status);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.status = :status")
    long countOfInsts(@Param("status") Instance.Status status);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.software.createdBy = :providerId AND i.status = :status")
    long countOfInsts(@Param("providerId") String providerId, @Param("status") Instance.Status status);

    @Query("SELECT COUNT(DISTINCT createdBy) FROM Software s WHERE s.status = :status")
    long countOfProviders(@Param("status") Software.Status status);

    @Query("SELECT COUNT(DISTINCT createdBy) FROM Instance i WHERE i.status = :status")
    long countOfUsers(@Param("status") Instance.Status status);

    @Query("SELECT COUNT(DISTINCT createdBy) FROM Instance i WHERE i.software.createdBy = :providerId AND i.status = :status")
    long countOfUsers(@Param("providerId") String providerId, @Param("status") Instance.Status status);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE "
            + "i.status = :status GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id DESC")
    List<Object[]> countsOfInsts(@Param("status") Instance.Status status, Pageable page);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.createdBy = :providerId AND "
            + "i.status = :status GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id DESC")
    List<Object[]> countsOfInsts(@Param("providerId") String providerId,
            @Param("status") Instance.Status status, Pageable page);

    @Query("SELECT s.createdBy, COUNT(*) FROM Software s WHERE "
            + "s.status = :status GROUP BY s.createdBy ORDER BY COUNT(*) DESC")
    List<Object[]> countsOfSwsProvider(@Param("status") Software.Status status, Pageable page);

    @Query("SELECT i.software.createdBy, COUNT(*) FROM Instance i WHERE "
            + "i.status = :status GROUP BY i.software.createdBy ORDER BY COUNT(*) DESC")
    List<Object[]> countsOfInstsProvider(@Param("status") Instance.Status status, Pageable page);

    @Query("SELECT i.createdBy, COUNT(*) FROM Instance i WHERE "
            + "i.status = :status GROUP BY i.createdBy ORDER BY COUNT(*) DESC")
    List<Object[]> countsOfInstsUser(@Param("status") Instance.Status status, Pageable page);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.usageStartDate >= :start AND i.usageStartDate < :end")
    long countOfInstsApproval(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.usageStartDate IS NOT NULL "
            + "AND ((i.usageStartDate <= :start AND (i.usageEndDate IS NULL OR i.usageEndDate < :end)) OR (i.usageStartDate >= :start AND i.usageStartDate < :end))")
    long countOfInstsUsing(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.createdBy = :createdBy "
            + "AND i.usageStartDate >= :start AND i.usageStartDate < :end")
    long countOfInstsUserApproval(@Param("createdBy") String createdBy, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(*) FROM Instance i WHERE i.createdBy = :createdBy " + "AND i.usageStartDate IS NOT NULL "
            + "AND ((i.usageStartDate <= :start AND (i.usageEndDate IS NULL OR i.usageEndDate < :end)) OR (i.usageStartDate >= :start AND i.usageStartDate < :end))")
    long countOfInstsUserUsing(@Param("createdBy") String createdBy, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.id IN :idIn "
            + "AND i.usageStartDate >= :start AND i.usageStartDate < :end "
            + "GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id ASC")
    List<Object[]> countsOfInstsApproval(@Param("idIn") List<Long> idIn, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.id IN :idIn "
            + "AND i.usageStartDate IS NOT NULL "
            + "AND ((i.usageStartDate <= :start AND (i.usageEndDate IS NULL OR i.usageEndDate < :end)) OR (i.usageStartDate >= :start AND i.usageStartDate < :end)) "
            + "GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id ASC")
    List<Object[]> countsOfInstsUsing(@Param("idIn") List<Long> idIn, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.createdBy = :providerId AND i.software.id IN :idIn "
            + "AND i.usageStartDate >= :start AND i.usageStartDate < :end "
            + "GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id ASC")
    List<Object[]> countsOfInstsApproval(@Param("providerId") String providerId, @Param("idIn") List<Long> idIn,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.createdBy = :providerId AND i.software.id IN :idIn "
            + "AND i.usageStartDate IS NOT NULL "
            + "AND ((i.usageStartDate <= :start AND (i.usageEndDate IS NULL OR i.usageEndDate < :end)) OR (i.usageStartDate >= :start AND i.usageStartDate < :end)) "
            + "GROUP BY i.software.id ORDER BY COUNT(*) DESC, i.software.id ASC")
    List<Object[]> countsOfInstsUsing(@Param("providerId") String providerId, @Param("idIn") List<Long> idIn,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.createdBy, COUNT(*) FROM Software s WHERE s.status = :status "
            + "GROUP BY s.createdBy ORDER BY COUNT(*) DESC, s.id DESC")
    List<Object[]> countsOfSwsGroupByProvider(@Param("status") Software.Status status, Pageable page);

    @Query("SELECT i.software.createdBy, COUNT(*) FROM Instance i WHERE i.status = :status "
            + "GROUP BY i.software.createdBy ORDER BY COUNT(*) DESC, i.software.id DESC")
    List<Object[]> countsOfInstsGroupByProvider(@Param("status") Instance.Status status, Pageable page);

    @Query("SELECT i.software.id, COUNT(*) FROM Instance i WHERE i.software.id IN :idIn "
            + "GROUP BY i.software.id")
    List<Object[]> countsOfSodInsts(@Param("idIn") List<Long> idIn);

    @Query("SELECT i.id, DATEDIFF(ifnull(i.usageEndDate, now()), i.usageStartDate) FROM Instance i WHERE i.createdBy = :providerId AND i.id IN :idIn")
    List<Object[]> dayOfUseInstsPeriod(@Param("providerId") String providerId, @Param("idIn") List<Long> idIn);

    default long countOfInsts(LocalDateTime start, LocalDateTime end, boolean using) {
        if (using) {
            return countOfInstsUsing(start, end);
        } else {
            return countOfInstsApproval(start, end);
        }
    }

    default long countOfInstsUser(String createdBy, LocalDateTime start, LocalDateTime end, boolean using) {
        if (using) {
            return countOfInstsUserApproval(createdBy, start, end);
        } else {
            return countOfInstsUserApproval(createdBy, start, end);
        }
    }

    default List<Object[]> countsOfInsts(List<Long> idIn, LocalDateTime start, LocalDateTime end, boolean using) {
        if (using) {
            return countsOfInstsUsing(idIn, start, end);
        } else {
            return countsOfInstsApproval(idIn, start, end);
        }
    }

    default List<Object[]> countsOfInsts(String providerId, List<Long> idIn, LocalDateTime start, LocalDateTime end,
            boolean using) {
        if (using) {
            return countsOfInstsUsing(providerId, idIn, start, end);
        } else {
            return countsOfInstsApproval(providerId, idIn, start, end);
        }
    }

}
