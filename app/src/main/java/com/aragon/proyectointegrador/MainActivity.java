package com.aragon.proyectointegrador;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private View cardCreditos, cardBlocNotas, cardArchivoSD, cardInventario;
    private Button btnBlocNotas, btnArchivoSD, btnInventario, btnCreditos;
    private Button btnCerrarCreditos, btnCerrarBloc, btnCerrarArchivoSD, btnCerrarInventario;

    // Bloc de notas (ejercicio 14)
    private EditText editNota;
    private TextView tvContador;
    private Button btnGuardarNota, btnLimpiarNota;
    private static final String NOMBRE_ARCHIVO_NOTAS = "notas.txt";

    // Archivo SD (ejercicio 15)
    private EditText editNombreArchivoSD, editContenidoSD;
    private Button btnGrabarSD, btnGrabarSDManual, btnRecuperarSD, btnRecuperarSDManual;

    // Inventario SQLite (ejercicio 16)
    private EditText editCodigo, editDescripcion, editPrecio, editStock;
    private Button btnAlta, btnConsultaCodigo, btnConsultaDescripcion, btnBaja, btnModificacion, btnMostrarTodos;
    private ListView listViewArticulos;
    private AdminSQLiteOpenHelper admin;
    private ArrayAdapter<String> adaptador;
    private List<String> listaArticulos;

    private static final int REQUEST_PERMISSION_WRITE_SD = 100;

    // Lanzador para guardar archivo manualmente (SAF)
    private final ActivityResultLauncher<Intent> saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    escribirEnUri(uri);
                }
            }
    );

    // Lanzador para abrir archivo manualmente (SAF)
    private final ActivityResultLauncher<Intent> openFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    leerDesdeUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        crearCanalNotificaciones();
        verificarPermisoSD();
        cargarNota();
        configurarContador();
        setupListeners();
        setupInventario();
    }

    private void initViews() {
        cardCreditos = findViewById(R.id.cardCreditos);
        cardBlocNotas = findViewById(R.id.cardBlocNotas);
        cardArchivoSD = findViewById(R.id.cardArchivoSD);
        cardInventario = findViewById(R.id.cardInventario);

        btnBlocNotas = findViewById(R.id.id_btn_bloc_notas);
        btnArchivoSD = findViewById(R.id.id_btn_archivo_sd);
        btnInventario = findViewById(R.id.id_btn_inventario);
        btnCreditos = findViewById(R.id.id_btn_creditos);

        btnCerrarCreditos = findViewById(R.id.btnCerrarCreditos);
        btnCerrarBloc = findViewById(R.id.btnCerrarBloc);
        btnCerrarArchivoSD = findViewById(R.id.btnCerrarArchivoSD);
        btnCerrarInventario = findViewById(R.id.btnCerrarInventario);

        editNota = findViewById(R.id.editNota);
        tvContador = findViewById(R.id.tvContador);
        btnGuardarNota = findViewById(R.id.btnGuardarNota);
        btnLimpiarNota = findViewById(R.id.btnLimpiarNota);

        editNombreArchivoSD = findViewById(R.id.editNombreArchivoSD);
        editContenidoSD = findViewById(R.id.editContenidoSD);
        btnGrabarSD = findViewById(R.id.btnGrabarSD);
        btnGrabarSDManual = findViewById(R.id.btnGrabarSDManual);
        btnRecuperarSD = findViewById(R.id.btnRecuperarSD);
        btnRecuperarSDManual = findViewById(R.id.btnRecuperarSDManual);

        editCodigo = findViewById(R.id.editCodigo);
        editDescripcion = findViewById(R.id.editDescripcion);
        editPrecio = findViewById(R.id.editPrecio);
        editStock = findViewById(R.id.editStock);
        btnAlta = findViewById(R.id.btnAlta);
        btnConsultaCodigo = findViewById(R.id.btnConsultaCodigo);
        btnConsultaDescripcion = findViewById(R.id.btnConsultaDescripcion);
        btnBaja = findViewById(R.id.btnBaja);
        btnModificacion = findViewById(R.id.btnModificacion);
        btnMostrarTodos = findViewById(R.id.btnMostrarTodos);
        listViewArticulos = findViewById(R.id.listViewArticulos);
    }

    private void setupListeners() {
        btnBlocNotas.setOnClickListener(v -> mostrarSeccion(cardBlocNotas));
        btnArchivoSD.setOnClickListener(v -> mostrarSeccion(cardArchivoSD));
        btnInventario.setOnClickListener(v -> mostrarSeccion(cardInventario));
        btnCreditos.setOnClickListener(v -> mostrarSeccion(cardCreditos));

        btnCerrarCreditos.setOnClickListener(v -> cardCreditos.setVisibility(View.GONE));
        btnCerrarBloc.setOnClickListener(v -> cardBlocNotas.setVisibility(View.GONE));
        btnCerrarArchivoSD.setOnClickListener(v -> cardArchivoSD.setVisibility(View.GONE));
        btnCerrarInventario.setOnClickListener(v -> cardInventario.setVisibility(View.GONE));

        btnGuardarNota.setOnClickListener(v -> grabarNota());
        btnLimpiarNota.setOnClickListener(v -> {
            editNota.setText("");
            tvContador.setText("0 caracteres");
        });

        btnGrabarSD.setOnClickListener(v -> grabarEnSD());
        btnGrabarSDManual.setOnClickListener(v -> iniciarGrabadoManual());
        btnRecuperarSD.setOnClickListener(v -> recuperarDeSD());
        btnRecuperarSDManual.setOnClickListener(v -> iniciarRecuperacionManual());

        btnAlta.setOnClickListener(v -> altaArticulo());
        btnConsultaCodigo.setOnClickListener(v -> consultaPorCodigo());
        btnConsultaDescripcion.setOnClickListener(v -> consultaPorDescripcion());
        btnBaja.setOnClickListener(v -> bajaPorCodigo());
        btnModificacion.setOnClickListener(v -> modificacionArticulo());
        btnMostrarTodos.setOnClickListener(v -> mostrarTodosArticulos());
    }

    private void mostrarSeccion(View seccion) {
        cardCreditos.setVisibility(View.GONE);
        cardBlocNotas.setVisibility(View.GONE);
        cardArchivoSD.setVisibility(View.GONE);
        cardInventario.setVisibility(View.GONE);
        seccion.setVisibility(View.VISIBLE);
        if (seccion == cardInventario) mostrarTodosArticulos();
    }

    // ---------- EJERCICIO 14: Bloc de notas (memoria interna) ----------
    private void configurarContador() {
        editNota.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvContador.setText(s.length() + " caracteres");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarNota() {
        File archivo = new File(getFilesDir(), NOMBRE_ARCHIVO_NOTAS);
        if (archivo.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
                StringBuilder sb = new StringBuilder();
                String linea;
                while ((linea = br.readLine()) != null) sb.append(linea).append("\n");
                editNota.setText(sb.toString());
            } catch (IOException e) {
                Toast.makeText(this, "Error al cargar nota", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void grabarNota() {
        try (FileWriter fw = new FileWriter(new File(getFilesDir(), NOMBRE_ARCHIVO_NOTAS))) {
            fw.write(editNota.getText().toString());
            Toast.makeText(this, "Nota guardada en memoria interna", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- EJERCICIO 15: Archivo en tarjeta SD ----------
    private void verificarPermisoSD() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
                                              android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_WRITE_SD);
            }
        }
        // En Android 13+ (API 33+), WRITE_EXTERNAL_STORAGE no se usa para archivos especificos de la app.
        // Las carpetas en Android/data/ son accesibles sin permisos especiales.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_SD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de SD concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sin permiso no se puede usar la tarjeta SD", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void grabarEnSD() {
        String nombre = editNombreArchivoSD.getText().toString().trim();
        String contenido = editContenidoSD.getText().toString().trim();
        if (nombre.isEmpty() || contenido.isEmpty()) {
            Toast.makeText(this, "Complete nombre y contenido", Toast.LENGTH_SHORT).show();
            return;
        }
        nombre = nombre.replace("/", "_").replace("\\", "_");

        File[] dirs = getExternalFilesDirs(null);
        File directorioDestino = null;
        
        // Debug para ver que esta viendo el sistema
        StringBuilder sb = new StringBuilder("Rutas halladas: " + dirs.length + "\n");
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i] != null) {
                sb.append(i).append(": ").append(dirs[i].getAbsolutePath()).append("\n");
                // Si encontramos algo que no sea emulated/0, lo preferimos
                if (!dirs[i].getAbsolutePath().contains("emulated/0")) {
                    directorioDestino = dirs[i];
                }
            }
        }
        
        if (directorioDestino == null && dirs.length > 0) {
            directorioDestino = dirs[0];
        }

        if (directorioDestino == null) {
            Toast.makeText(this, "No se detecto ningun almacenamiento", Toast.LENGTH_SHORT).show();
            return;
        }

        File archivo = new File(directorioDestino, nombre);
        try (FileWriter fw = new FileWriter(archivo)) {
            fw.write(contenido);
            String rutaFinal = archivo.getAbsolutePath();
            
            if (rutaFinal.contains("emulated/0")) {
                Toast.makeText(this, "AVISO: Guardado en Interna (No se detecto la SD)", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "EXITO: Guardado en SD Externa", Toast.LENGTH_LONG).show();
            }
            
            editNombreArchivoSD.setHint(rutaFinal);
            editNombreArchivoSD.setText("");
            editContenidoSD.setText("");
        } catch (IOException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- GRABADO MANUAL (SAF) ----------
    private void iniciarGrabadoManual() {
        String nombre = editNombreArchivoSD.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese un nombre de archivo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nombre.contains(".")) nombre += ".txt";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, nombre);
        saveFileLauncher.launch(intent);
    }

    private void escribirEnUri(Uri uri) {
        String contenido = editContenidoSD.getText().toString();
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(contenido);
            Toast.makeText(this, "Archivo guardado exitosamente", Toast.LENGTH_SHORT).show();
            editNombreArchivoSD.setText("");
            editContenidoSD.setText("");
        } catch (IOException e) {
            Toast.makeText(this, "Error al escribir el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarRecuperacionManual() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        openFileLauncher.launch(intent);
    }

    private void leerDesdeUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea).append("\n");
            }
            editContenidoSD.setText(sb.toString());
            Toast.makeText(this, "Archivo cargado exitosamente", Toast.LENGTH_SHORT).show();
            
            // Opcional: intentar obtener el nombre del archivo del URI
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    editNombreArchivoSD.setText(cursor.getString(nameIndex));
                }
                cursor.close();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void recuperarDeSD() {
        String nombre = editNombreArchivoSD.getText().toString().trim();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingrese el nombre del archivo", Toast.LENGTH_SHORT).show();
            return;
        }
        nombre = nombre.replace("/", "_").replace("\\", "_");

        File[] dirs = getExternalFilesDirs(null);
        File archivoEncontrado = null;

        // Buscar el archivo en todas las rutas de almacenamiento posibles (interna y externa)
        for (File dir : dirs) {
            if (dir != null) {
                File archivoPosible = new File(dir, nombre);
                if (archivoPosible.exists()) {
                    archivoEncontrado = archivoPosible;
                    break;
                }
            }
        }

        if (archivoEncontrado == null) {
            Toast.makeText(this, "El archivo no existe en ningun almacenamiento", Toast.LENGTH_SHORT).show();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(archivoEncontrado))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea).append("\n");
            editContenidoSD.setText(sb.toString());
            Toast.makeText(this, "Recuperado de: " + archivoEncontrado.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al leer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- EJERCICIO 16: SQLite con stock y notificaciones ----------
    private void setupInventario() {
        admin = new AdminSQLiteOpenHelper(this);
        listaArticulos = new ArrayList<>();
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaArticulos);
        listViewArticulos.setAdapter(adaptador);
    }

    private void altaArticulo() {
        String codStr = editCodigo.getText().toString().trim();
        String desc = editDescripcion.getText().toString().trim();
        String precioStr = editPrecio.getText().toString().trim();
        String stockStr = editStock.getText().toString().trim();

        if (codStr.isEmpty() || desc.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        int codigo = Integer.parseInt(codStr);
        double precio = Double.parseDouble(precioStr);
        int stock = Integer.parseInt(stockStr);

        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("codigo", codigo);
        values.put("descripcion", desc);
        values.put("precio", precio);
        values.put("stock", stock);

        long resultado = db.insert("articulos", null, values);
        db.close();

        if (resultado != -1) {
            Toast.makeText(this, "Articulo agregado", Toast.LENGTH_SHORT).show();
            limpiarCamposInventario();
            if (stock < 5) {
                enviarNotificacion("Stock Bajo", "Articulo " + codigo + " tiene stock: " + stock);
            }
            mostrarTodosArticulos();
        } else {
            Toast.makeText(this, "Error: el código ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    private void consultaPorCodigo() {
        String codStr = editCodigo.getText().toString().trim();
        if (codStr.isEmpty()) {
            Toast.makeText(this, "Ingrese un código", Toast.LENGTH_SHORT).show();
            return;
        }
        int codigo = Integer.parseInt(codStr);
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor fila = db.rawQuery("SELECT descripcion, precio, stock FROM articulos WHERE codigo=" + codigo, null);
        if (fila.moveToFirst()) {
            String desc = fila.getString(0);
            double precio = fila.getDouble(1);
            int stock = fila.getInt(2);
            String mensaje = "Codigo: " + codigo + "\nDescripcion: " + desc + "\nPrecio: $" + precio + "\nStock: " + stock;
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            editDescripcion.setText(desc);
            editPrecio.setText(String.valueOf(precio));
            editStock.setText(String.valueOf(stock));
            if (stock < 5) {
                enviarNotificacion("Stock Bajo", "Articulo " + codigo + " (" + desc + ") tiene solo " + stock + " unidades.");
            }
        } else {
            Toast.makeText(this, "No existe articulo con ese codigo", Toast.LENGTH_SHORT).show();
            limpiarCamposInventario();
        }
        fila.close();
        db.close();
    }

    private void consultaPorDescripcion() {
        String desc = editDescripcion.getText().toString().trim();
        if (desc.isEmpty()) {
            Toast.makeText(this, "Ingrese una descripción", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor fila = db.rawQuery("SELECT codigo, precio, stock FROM articulos WHERE descripcion='" + desc + "'", null);
        if (fila.moveToFirst()) {
            int codigo = fila.getInt(0);
            double precio = fila.getDouble(1);
            int stock = fila.getInt(2);
            String mensaje = "Codigo: " + codigo + "\nDescripcion: " + desc + "\nPrecio: $" + precio + "\nStock: " + stock;
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            editCodigo.setText(String.valueOf(codigo));
            editPrecio.setText(String.valueOf(precio));
            editStock.setText(String.valueOf(stock));
            if (stock < 5) {
                enviarNotificacion("Stock Bajo", "Articulo " + codigo + " (" + desc + ") tiene solo " + stock + " unidades.");
            }
        } else {
            Toast.makeText(this, "No existe articulo con esa descripcion", Toast.LENGTH_SHORT).show();
            limpiarCamposInventario();
        }
        fila.close();
        db.close();
    }

    private void bajaPorCodigo() {
        String codStr = editCodigo.getText().toString().trim();
        if (codStr.isEmpty()) {
            Toast.makeText(this, "Ingrese el codigo a eliminar", Toast.LENGTH_SHORT).show();
            return;
        }
        int codigo = Integer.parseInt(codStr);
        SQLiteDatabase db = admin.getWritableDatabase();
        int cant = db.delete("articulos", "codigo=" + codigo, null);
        db.close();
        if (cant == 1) {
            Toast.makeText(this, "Articulo eliminado", Toast.LENGTH_SHORT).show();
            limpiarCamposInventario();
            mostrarTodosArticulos();
        } else {
            Toast.makeText(this, "No existe articulo con ese codigo", Toast.LENGTH_SHORT).show();
        }
    }

    private void modificacionArticulo() {
        String codStr = editCodigo.getText().toString().trim();
        String desc = editDescripcion.getText().toString().trim();
        String precioStr = editPrecio.getText().toString().trim();
        String stockStr = editStock.getText().toString().trim();

        if (codStr.isEmpty() || desc.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos para modificar", Toast.LENGTH_SHORT).show();
            return;
        }

        int codigo = Integer.parseInt(codStr);
        double precio = Double.parseDouble(precioStr);
        int stock = Integer.parseInt(stockStr);

        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("descripcion", desc);
        valores.put("precio", precio);
        valores.put("stock", stock);
        int cant = db.update("articulos", valores, "codigo=" + codigo, null);
        db.close();

        if (cant == 1) {
            Toast.makeText(this, "Articulo modificado", Toast.LENGTH_SHORT).show();
            if (stock < 5) {
                enviarNotificacion("Stock Bajo", "El articulo " + codigo + " ahora tiene stock: " + stock);
            }
            mostrarTodosArticulos();
        } else {
            Toast.makeText(this, "No existe articulo con ese codigo", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarTodosArticulos() {
        listaArticulos.clear();
        SQLiteDatabase db = admin.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT codigo, descripcion, precio, stock FROM articulos", null);
        boolean hayStockBajo = false;
        while (cursor.moveToNext()) {
            int codigo = cursor.getInt(0);
            String desc = cursor.getString(1);
            double precio = cursor.getDouble(2);
            int stock = cursor.getInt(3);
            listaArticulos.add("Cod: " + codigo + " | " + desc + " | $" + precio + " | Stock: " + stock);
            if (stock < 5) hayStockBajo = true;
        }
        adaptador.notifyDataSetChanged();
        cursor.close();
        db.close();
        if (hayStockBajo) {
            enviarNotificacion("Inventario", "Hay articulos con stock bajo (menos de 5 unidades).");
        }
        if (listaArticulos.isEmpty()) {
            Toast.makeText(this, "No hay articulos registrados", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCamposInventario() {
        editCodigo.setText("");
        editDescripcion.setText("");
        editPrecio.setText("");
        editStock.setText("");
    }

    // ---------- NOTIFICACIONES ----------
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "canal_stock",
                    "Alertas de Stock",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones cuando el stock es menor a 5 unidades");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void enviarNotificacion(String titulo, String contenido) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "canal_stock")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(contenido)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                manager.notify((int) System.currentTimeMillis(), builder.build());
            } else {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1002);
            }
        } else {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}