# 🚀 Projek Task Management App

Selamat datang di **Projek Task Management App**! Sebuah aplikasi manajemen tugas desktop yang dirancang dengan cermat menggunakan JavaFX untuk membantu Anda mengelola setiap aspek pekerjaan dan hidup Anda dengan lebih efisien. Ucapkan selamat tinggal pada daftar tugas yang berantakan dan sambutlah produktivitas yang terorganisir dengan antarmuka yang intuitif, notifikasi cerdas, dan pelacakan progres yang mendalam.

## ✨ Fitur-Fitur Unggulan

Aplikasi ini hadir dengan serangkaian fitur yang dirancang untuk mempermudah alur kerja manajemen tugas Anda:

* **Manajemen Tugas Induk & Sub-tugas yang Komprehensif:** Organisasikan proyek besar menjadi tugas-tugas yang lebih kecil dan mudah dikelola. Buat tugas utama dan pecah lagi menjadi sub-tugas yang terperinci, memastikan tidak ada detail yang terlewat.
* **Pelacakan Progres Visual yang Dinamis:** Pantau kemajuan setiap tugas dengan slider progres interaktif (0-100%). Aplikasi secara cerdas akan menandai tugas sebagai "Selesai" secara otomatis saat mencapai 100%, memberikan umpan balik visual instan.
* **Sistem Prioritas yang Jelas:** Klasifikasikan tugas berdasarkan tingkat urgensinya dengan opsi prioritas "Rendah", "Sedang", dan "Tinggi". Ini membantu Anda fokus pada hal-hal yang paling penting terlebih dahulu.
* **Deadline Tepat Waktu:** Setiap tugas dilengkapi dengan kemampuan untuk menentukan tanggal dan waktu jatuh tempo yang spesifik, membantu Anda tetap pada jadwal dan memenuhi komitmen.
* **Notifikasi Pengingat Cerdas:** Jangan pernah melewatkan deadline lagi! Aplikasi ini mengirimkan notifikasi desktop proaktif sebelum tugas jatuh tempo. Anda dapat menyesuaikan berapa hari sebelumnya pengingat ini muncul.
* **Pengelolaan Lampiran yang Terintegrasi:** Sertakan dokumen, gambar, atau file relevan lainnya langsung ke dalam tugas Anda. Lampiran dapat dibuka dengan mudah dari dalam aplikasi, menjaga semua informasi penting tetap di satu tempat.
* **Dashboard Interaktif & Informatif:** Dapatkan gambaran umum kinerja dan beban kerja Anda secara sekilas. Dashboard menampilkan statistik tugas (total, selesai, aktif) dan menyajikan daftar tugas-tugas terdekat yang akan datang.
* **Sistem Autentikasi Pengguna yang Aman:** Setiap pengguna memiliki akun pribadi, memastikan data tugas Anda aman dan terpersonalisasi. Proses registrasi dan login yang mudah membuat Anda siap dalam hitungan detik.
* **Navigasi Sidebar yang Efisien:** Beralih dengan mulus antar tampilan dashboard, daftar semua tugas, dan daftar tugas yang sudah selesai melalui sidebar yang terorganisir.

## 🛠️ Teknologi yang Digunakan

Proyek ini dibangun di atas fondasi teknologi yang solid dan modern:

* **Bahasa Pemrograman:** Java 17
* **Framework UI:** JavaFX 17.0.6 - Menyediakan komponen UI yang kaya dan interaktif.
* **Build Tool:** Apache Maven - Untuk otomatisasi proses build dan manajemen dependensi.
* **Database:** SQLite - Database ringan, efisien, dan tanpa server untuk penyimpanan data lokal.
* **Ikon:** Ikonli (dengan FontAwesomeSolid) - Menyediakan koleksi ikon yang luas untuk mempercantik antarmuka.
* **Komponen UI Tambahan:** ControlsFX - Meningkatkan pengalaman pengguna dengan widget UI tambahan.

## ⚙️ Persyaratan Sistem

Untuk menjalankan aplikasi ini dengan lancar, pastikan sistem Anda memenuhi persyaratan berikut:

* **Java Development Kit (JDK) 17** atau versi yang lebih baru terinstal.
* **Apache Maven** terinstal dan dikonfigurasi pada sistem PATH Anda.
* **Sistem Operasi:** Aplikasi telah berhasil diuji pada lingkungan Windows. Namun, karena JavaFX bersifat lintas platform, seharusnya berfungsi dengan baik pada sistem operasi Linux dan macOS yang mendukung JavaFX dan notifikasi desktop.

## 🚀 Instalasi dan Menjalankan Proyek

Ikuti langkah-langkah detail di bawah ini untuk mengkloning repositori, membangun, dan menjalankan aplikasi di mesin lokal Anda:

1.  **Kloning Repositori:**
    Buka terminal atau command prompt Anda dan jalankan perintah berikut untuk mengkloning proyek ke komputer Anda:
    ```bash
    git clone [https://github.com/abidzan12/projek-task-managemant-app.git](https://github.com/abidzan12/projek-task-managemant-app.git)
    # Arahkan ke direktori root proyek
    cd projek-task-managemant-app/projek-task-managemant-app-5fd6aa3abe6aa433e6b837aac157fd8f774baf06/
    ```

2.  **Inisialisasi Database:**
    Proyek ini menggunakan SQLite sebagai database lokal. Sebelum menjalankan aplikasi, Anda perlu memastikan database dan tabel yang diperlukan sudah dibuat. Jalankan perintah Maven berikut dari direktori root proyek:
    ```bash
    mvn clean compile exec:java -Dexec.mainClass="com.example.project.util.DatabaseInitializer"
    ```
    Perintah ini akan secara otomatis membuat file database `task.db` di dalam folder `data/` (jika belum ada) dan menginisialisasi skema tabel `tasks` serta tabel `users`.

3.  **Membangun Proyek dengan Maven:**
    Setelah database siap, gunakan Maven untuk membersihkan proyek, mengompilasi kode sumber, dan mengemas aplikasi. Konfigurasi `pom.xml` juga akan membuat *runtime image* mandiri menggunakan `jlink`, yang mencakup Java Runtime Environment (JRE) dan modul JavaFX yang dibutuhkan, sehingga aplikasi dapat berjalan tanpa instalasi JRE eksternal.
    ```bash
    mvn clean install
    ```
    Proses build mungkin memerlukan beberapa saat. Setelah berhasil, Anda akan menemukan folder `task-manager-runtime` yang berisi aplikasi mandiri di dalam direktori `target/`.

4.  **Menjalankan Aplikasi:**
    Navigasi ke direktori `bin` di dalam *runtime image* yang baru dibuat dan jalankan launcher aplikasi.
    ```bash
    cd target/task-manager-runtime/bin/
    # Untuk Linux/macOS:
    ./taskmanager
    # Untuk Windows:
    taskmanager.bat
    ```
    *Alternatif (Jika konfigurasi jlink tidak digunakan atau Anda ingin menjalankan JAR secara langsung):*
    Jika `pom.xml` Anda dikonfigurasi untuk menghasilkan JAR yang dapat dieksekusi tanpa `jlink` launcher khusus, Anda bisa menjalankan aplikasi langsung dari file JAR:
    ```bash
    java -jar target/project-1.0-SNAPSHOT.jar
    ```
    *(Catatan: Pastikan Anda memiliki JavaFX SDK yang sesuai yang diinstal secara eksternal atau disertakan dalam classpath jika menjalankan JAR tanpa `jlink`.)*

## 📁 Struktur Proyek Penting

--struktur proyek untuk navigasi dan pengembangan yang lebih mudah:

* `src/main/java/com/example/project/App.java`: Titik masuk utama aplikasi JavaFX Anda, menginisialisasi UI dan memuat scene awal.
* `src/main/java/com/example/project/model/Database.java`: Kelas penting yang mengelola semua operasi database, termasuk autentikasi pengguna, penyimpanan, pembaruan, dan pengambilan tugas.
* `src/main/java/com/example/project/model/Task.java`: Model data inti yang merepresentasikan objek tugas, lengkap dengan properti seperti nama, deskripsi, prioritas, progres, dan lampiran.
* `src/main/java/com/example/project/util/DatabaseInitializer.java`: Utilitas praktis untuk menyiapkan skema database SQLite, memastikan struktur yang benar untuk tabel `tasks` dan `users`.
* `src/main/resources/com/example/project/fxml/`: Direktori ini menampung semua file FXML yang mendefinisikan struktur antarmuka pengguna aplikasi.
* `src/main/resources/com/example/project/css/style.css`: File stylesheet CSS yang mengatur tampilan dan nuansa (look and feel) seluruh aplikasi, dari tata letak hingga warna dan font.
* `data/`: Direktori penting ini berfungsi sebagai penyimpanan untuk file database SQLite (`task.db`) dan folder `attachments/` tempat semua lampiran tugas disimpan.


## 📄 Lisensi

Hak Cipta (c) 2025 M. Abidzan Al-Ghifari
