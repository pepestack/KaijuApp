package cl.kaiju.kaijuapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.kaiju.kaijuapp.model.Movimiento
import cl.kaiju.kaijuapp.model.Producto
import cl.kaiju.kaijuapp.model.Usuario

@Database(entities = [Producto::class, Movimiento::class, Usuario::class], version = 2, exportSchema = false)
abstract class KaijuDatabase : RoomDatabase() {
    abstract fun inventarioDao(): InventarioDao

    companion object {
        // v2 agrega el código de barras sin borrar el inventario ya guardado en el teléfono.
        // El nombre del índice es el que Room genera; si no coincide falla al abrir la base.
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE productos ADD COLUMN codigoBarras TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_productos_codigoBarras ON productos (codigoBarras)")
            }
        }

        @Volatile private var instancia: KaijuDatabase? = null

        fun obtenerInstancia(context: Context): KaijuDatabase = instancia ?: synchronized(this) {
            instancia ?: crear(context, "kaiju.db").also { instancia = it }
        }

        /** También permite probar una base separada, sin tocar el inventario de la aplicación. */
        fun crear(context: Context, nombre: String): KaijuDatabase = Room.databaseBuilder(
            context.applicationContext, KaijuDatabase::class.java, nombre
        ).addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // onCreate ocurre solo al crear el archivo, dentro de la transacción de creación.
                // Los parámetros enlazados evitan concatenar datos en las sentencias SQL.
                DatosDePrueba.usuarios.forEach { usuario ->
                    db.execSQL("INSERT INTO usuarios (id, nombre, rol) VALUES (?, ?, ?)",
                        arrayOf<Any?>(usuario.id, usuario.nombre, usuario.rol.name))
                }
                DatosDePrueba.productos.forEach { producto ->
                    db.execSQL("""INSERT INTO productos
                        (codigo, nombre, descripcion, categoria, tipo, precio, stockActual, stockMinimo, detalle, codigoBarras)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        arrayOf<Any?>(producto.codigo, producto.nombre, producto.descripcion,
                            producto.categoria, producto.tipo, producto.precio, producto.stockActual,
                            producto.stockMinimo, producto.detalle, producto.codigoBarras))
                }
            }
        }).addMigrations(MIGRACION_1_2).build()
    }
}
