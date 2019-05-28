package org.inventivetalent.trashapp.common.db;

import androidx.room.*;

import java.util.List;

@Dao
public interface TrashcanDao {

	@Query("SELECT * FROM trashcans")
	List<TrashcanEntity> getAll();

	@Query("SELECT * FROM trashcans WHERE lat BETWEEN :minLat AND :maxLat AND lon BETWEEN :minLon AND :maxLon")
	List<TrashcanEntity> getAllInArea(double minLat, double maxLat, double minLon, double maxLon);

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertAll(TrashcanEntity... trashcans);

	@Delete
	void delete(TrashcanEntity trashcan);

	@Query("DELETE FROM trashcans")
	void deleteAll();

}
