package application.android.refresh.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import application.android.refresh.data.db.entity.Card

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE userId = :userId")
    fun getCards(userId: String): LiveData<List<Card>>

    @Query("SELECT * FROM cards WHERE userId = :userId")
    fun getCardsAtOnce(userId: String): List<Card>

    @Query("SELECT * FROM cards where layoutId = :layoutId")
    fun getCardsWithLayoutId(layoutId: Long): List<Card>

    @Query("SELECT COUNT(id) FROM cards WHERE layoutId = :layoutId")
    fun  cardsCountWithLayoutId(layoutId: Long): Int

    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCard(cardId: Long): LiveData<Card>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardAtOnce(cardId: Long): Card

    @Query("SELECT * FROM cards WHERE userId = :userId AND front LIKE '%' || :searchText || '%' OR back LIKE '%' || :searchText || '%' OR backExtra LIKE '%' || :searchText || '%'")
    fun searchCardsByText(searchText: String, userId: String): LiveData<List<Card>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(card: Card)

    @Update
    fun update(card: Card)

    @Delete
    fun delete(card: Card)
}