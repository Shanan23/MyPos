package id.tugas.pos.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import id.tugas.pos.data.model.Expense;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.data.model.Store;
import id.tugas.pos.data.model.Transaction;
import id.tugas.pos.data.model.TransactionItem;
import id.tugas.pos.data.model.User;
import id.tugas.pos.data.model.Saving;
import id.tugas.pos.data.database.SavingDao;
import id.tugas.pos.data.model.StockIn;
import id.tugas.pos.data.database.StockInDao;
import id.tugas.pos.data.model.ModalAwal;
import id.tugas.pos.data.database.ModalAwalDao;

@Database(entities = {
        User.class,
        Product.class,
        Transaction.class,
        TransactionItem.class,
        Expense.class,
        Store.class,
        Saving.class,
        StockIn.class,
        ModalAwal.class
}, version = 8, exportSchema = false)
public abstract class PosDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "pos_database";
    private static PosDatabase instance;
    
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();
    public abstract TransactionItemDao transactionItemDao();
    public abstract ExpenseDao expenseDao();
    public abstract StoreDao storeDao();
    public abstract SavingDao savingDao();
    public abstract StockInDao stockInDao();
    public abstract ModalAwalDao modalAwalDao();
    
    public static synchronized PosDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PosDatabase.class,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION_8_9)
                    .build();
        }
        return instance;
    }

    // Migrasi dari versi 8 ke 9: tambah kolom storeId di tabel saving
    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE saving ADD COLUMN storeId INTEGER NOT NULL DEFAULT 0");
        }
    };
} 