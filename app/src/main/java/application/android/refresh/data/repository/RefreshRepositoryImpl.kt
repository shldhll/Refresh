package application.android.refresh.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import application.android.refresh.data.db.*
import application.android.refresh.data.db.entity.Card
import application.android.refresh.data.db.entity.Layout
import application.android.refresh.data.db.entity.Routine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RefreshRepositoryImpl(
    private val layoutDao: LayoutDao,
    private val cardDao: CardDao,
    private val routineDao: RoutineDao,
    private val db: FirebaseFirestore
) : RefreshRepository {

    override suspend fun addLayout(layout: Layout) {
        withContext(Dispatchers.IO) {
            layoutDao.insert(layout)
            val layoutEntry = hashMapOf(
                "id" to layout.id,
                "name" to layout.name,
                "front" to layout.front,
                "back" to layout.back,
                "backExtra" to layout.backExtra
            )
            db.collection("data/${layout.userId}/layouts").document("${layout.id}").set(layoutEntry)
        }
    }

    override suspend fun updateLayout(layout: Layout) {
        withContext(Dispatchers.IO) {
            val id = layout.id
            getCardsWithLayoutId(id).map {
                val card = Card(
                    it.id, it.userId, it.layoutId, it.layoutName, it.front, it.back,
                    layout
                        .backExtra, it.backExtra
                )
                updateCard(card)
            }
            layoutDao.update(layout)
            val layoutEntry = hashMapOf(
                "id" to layout.id,
                "name" to layout.name,
                "front" to layout.front,
                "back" to layout.back,
                "backExtra" to layout.backExtra
            )
            db.collection("data/${layout.userId}/layouts").document("${layout.id}").set(layoutEntry)

        }
    }

    override suspend fun getCardsAtOnce(): List<Card> {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.getCardsAtOnce(UserUtils.getUserId())
        }
    }

    override suspend fun deleteLayout(layout: Layout) {
        withContext(Dispatchers.IO) {
            val id = layout.id
            getCardsWithLayoutId(id).map {
                deleteCard(it)
            }
            layoutDao.delete(layout)
            db.collection("data/${layout.userId}/layouts").document("${layout.id}").delete()
        }
    }

    override suspend fun getLayout(layoutId: Long): LiveData<Layout> {
        return withContext(Dispatchers.IO) {
            return@withContext layoutDao.getLayout(layoutId)
        }
    }

    override suspend fun getLayoutList(): LiveData<List<Layout>> {
        return withContext(Dispatchers.IO) {
            return@withContext layoutDao.getLayouts(UserUtils.getUserId())
        }
    }

    override suspend fun getLayoutsAtOnce(): List<Layout> {
        return withContext(Dispatchers.IO) {
            return@withContext layoutDao.getLayoutsAtOnce(UserUtils.getUserId())
        }
    }

    override suspend fun searchLayoutByText(searchText: String): LiveData<List<Layout>> {
        return withContext(Dispatchers.IO) {
            return@withContext layoutDao.searchLayoutsByText(searchText, UserUtils.getUserId())
        }
    }

    override suspend fun cardsCountWithLayoutId(layoutId: Long): Int {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.cardsCountWithLayoutId(layoutId)
        }
    }


    override suspend fun addCard(card: Card) {
        withContext(Dispatchers.IO) {
            cardDao.insert(card)
            val cardEntry = hashMapOf(
                "id" to card.id,
                "layoutId" to card.layoutId,
                "layoutName" to card.layoutName,
                "front" to card.front,
                "back" to card.back,
                "backExtraTitle" to card.backExtraTitle,
                "backExtra" to card.backExtra
            )
            db.collection("data/${card.userId}/cards").document("${card.id}").set(cardEntry)

        }
    }

    override suspend fun updateCard(card: Card) {
        withContext(Dispatchers.IO) {
            cardDao.update(card)
            val cardEntry = hashMapOf(
                "id" to card.id,
                "layoutId" to card.layoutId,
                "layoutName" to card.layoutName,
                "front" to card.front,
                "back" to card.back,
                "backExtraTitle" to card.backExtraTitle,
                "backExtra" to card.backExtra
            )
            db.collection("data/${card.userId}/cards").document("${card.id}").set(cardEntry)

        }
    }

    override suspend fun deleteCard(card: Card) {
        withContext(Dispatchers.IO) {
            cardDao.delete(card)
            db.collection("data/${card.userId}/cards").document("${card.id}").delete()

        }
    }

    override suspend fun getCardsWithLayoutId(layoutId: Long): List<Card> {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.getCardsWithLayoutId(layoutId)
        }
    }

    override suspend fun searchCardsByText(searchText: String): LiveData<List<Card>> {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.searchCardsByText(searchText, UserUtils.getUserId())
        }
    }

    override suspend fun getCardList(): LiveData<List<Card>> {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.getCards(UserUtils.getUserId())
        }
    }

    override suspend fun getCard(id: Long): LiveData<Card> {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.getCard(id)
        }
    }

    override suspend fun addRoutine(routine: Routine) {
        withContext(Dispatchers.IO) {
            routineDao.insert(routine)
            val routineEntry = hashMapOf(
                "id" to routine.id,
                "name" to routine.name,
                "layoutIds" to Converters().longListToString(routine.layoutIds),
                "finishedCards" to Converters().routineCardToJson(routine.finishedCards)
            )
            db.collection("data/${routine.userId}/routines").document("${routine.id}")
                .set(routineEntry)
        }
    }

    override suspend fun deleteRoutine(routine: Routine) {
        withContext(Dispatchers.IO) {
            routineDao.delete(routine)
            db.collection("data/${routine.userId}/routines").document("${routine.id}").delete()
        }
    }

    override suspend fun getRoutine(id: Long): LiveData<Routine> {
        return withContext(Dispatchers.IO) {
            return@withContext routineDao.getRoutine(id)
        }
    }

    override suspend fun getRoutineList(): LiveData<List<Routine>> {
        return withContext(Dispatchers.IO) {
            return@withContext routineDao.getRoutines(UserUtils.getUserId())
        }
    }

    override suspend fun getLayoutAtOnce(id: Long): Layout {
        return withContext(Dispatchers.IO) {
            return@withContext layoutDao.getLayoutAtOnce(id)
        }
    }

    override suspend fun updateRoutine(routine: Routine) {
        withContext(Dispatchers.IO) {
            routineDao.update(routine)
        }
    }

    override suspend fun getCardAtOnce(id: Long): Card {
        return withContext(Dispatchers.IO) {
            return@withContext cardDao.getCardAtOnce(id)
        }
    }
// Firebase data sync
    // Firebase data sync
    override suspend fun getDataFromDB(
        userId: String,
        layouts: ArrayList<Layout>,
        cards: ArrayList<Card>,
        routines: ArrayList<Routine>
    ): LiveData<Boolean> {
        return withContext(Dispatchers.IO) {
            val isSuccessful = MutableLiveData<Boolean>()

            getLayoutsFromDB(userId, layouts, cards, routines, isSuccessful)

            return@withContext isSuccessful
        }
    }

    private fun getLayoutsFromDB(
        userId: String,
        layouts: ArrayList<Layout>,
        cards: ArrayList<Card>,
        routines: ArrayList<Routine>,
        isSuccessful: MutableLiveData<Boolean>
    ) {
        val source = Source.SERVER
        db.collection("data/$userId/layouts")
            .get(source)
            .addOnSuccessListener { result ->
                for (document in result) {
                    val layout = Layout(
                        document.data["id"].toString().toLong(),
                        userId,
                        document.data["name"].toString(),
                        document.data["front"].toString(),
                        document.data["back"].toString(),
                        document.data["backExtra"].toString()
                    )
                    layouts.add(layout)
                }
                getCardsFromDB(userId, cards, routines, isSuccessful)
            }
            .addOnFailureListener {
                isSuccessful.postValue(false)
            }
    }

    private fun getCardsFromDB(
        userId: String,
        cards: ArrayList<Card>,
        routines: ArrayList<Routine>, isSuccessful:
        MutableLiveData<Boolean>
    ) {
        val source = Source.SERVER
        db.collection("data/$userId/cards")
            .get(source)
            .addOnSuccessListener { result ->
                for (document in result) {
                    val card = Card(
                        document.data["id"].toString().toLong(),
                        userId,
                        document.data["layoutId"].toString().toLong(),
                        document.data["layoutName"].toString(),
                        document.data["front"].toString(),
                        document.data["back"].toString(),
                        document.data["backExtraTitle"].toString(),
                        document.data["backExtra"].toString()
                    )
                    cards.add(card)
                }
                getRoutinesFromDB(userId, routines, isSuccessful)
            }
            .addOnFailureListener {
                isSuccessful.postValue(false)
            }
    }

    private fun getRoutinesFromDB(
        userId: String,
        routines: ArrayList<Routine>, isSuccessful:
        MutableLiveData<Boolean>
    ) {
        val source = Source.SERVER
        db.collection("data/$userId/routines")
            .get(source)
            .addOnSuccessListener { result ->
                for (document in result) {
                    val routine = Routine(
                        document.data["id"].toString().toLong(),
                        userId,
                        document.data["name"].toString(),
                        Converters().stringToLongList(document.data["layoutIds"].toString()),
                        Converters().jsonToRoutineCard(document.data["finishedCards"].toString())
                    )
                    routines.add(routine)
                }
                isSuccessful.postValue(true)
            }
            .addOnFailureListener {
                isSuccessful.postValue(false)
            }
    }
}
