package id.tugas.pos.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import id.tugas.pos.R;
import id.tugas.pos.data.model.User;
import id.tugas.pos.ui.dashboard.DashboardFragment;
import id.tugas.pos.ui.expense.ExpenseFragment;
import id.tugas.pos.ui.history.HistoryFragment;
import id.tugas.pos.ui.login.LoginActivity;
import id.tugas.pos.ui.produk.ProdukFragment;
import id.tugas.pos.ui.report.ReportFragment;
import id.tugas.pos.ui.settings.SettingsFragment;
import id.tugas.pos.ui.transaksi.TransaksiFragment;
import id.tugas.pos.ui.user.UserManagementFragment;
import id.tugas.pos.viewmodel.LoginViewModel;
import android.util.Log;
import android.widget.Spinner; // Tambahkan import untuk Spinner
import id.tugas.pos.data.model.Store;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView tvUserName, tvUserRole;
    private LoginViewModel loginViewModel;
    private User currentUser;
    public Spinner spinnerStore;
    public TextView labelStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // Observe currentUser and jump to login if null
        loginViewModel.getCurrentUser().observe(this, user -> {
            Log.d(TAG, "MainActivity: currentUser changed: " + new Gson().toJson(user));
            if (user == null) {
                Log.d(TAG, "MainActivity: currentUser is null, navigating to login");
                navigateToLogin();
            } else {
                currentUser = user;
                setupUserInfo();
                
                // Load default fragment setelah currentUser tersedia
                if (savedInstanceState == null) {
                    // Load default fragment based on user role
                    if (loginViewModel.isAdmin()) {
                        Log.d(TAG, "MainActivity: User is admin, loading Dashboard");
                        loadFragment(new DashboardFragment());
                        getSupportActionBar().setTitle("Dashboard");
                    } else {
                        Log.d(TAG, "MainActivity: User is not admin, loading Transaksi");
                        loadFragment(new TransaksiFragment());
                        getSupportActionBar().setTitle("Transaksi");
                    }
                }
            }
        });
        
        // Check if user is logged in (pakai SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            navigateToLogin();
            return;
        }
        
        // Initialize views
        initViews();
        setupToolbar();
        setupNavigation();

        // HAPUS: Logic default fragment dari sini karena sudah dipindah ke observer
    }
    
    private void initViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        
        // Get header views
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        tvUserRole = headerView.findViewById(R.id.tvUserRole);
        
        // Inisialisasi spinnerStore dan labelStore dari toolbar
        spinnerStore = toolbar.findViewById(R.id.spinnerStore);
        labelStore = toolbar.findViewById(R.id.labelStore);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Dashboard");
        getSupportActionBar().setSubtitle(null); // Clear subtitle
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }
    
    // Method untuk mengatur title dan subtitle dari fragment
    public void setToolbarTitle(String title, String subtitle) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle(subtitle);
        }
    }
    
    // Method untuk setup spinner toolbar
    public void setupToolbarStoreSpinner(
            java.util.List<Store> stores,
            android.widget.AdapterView.OnItemSelectedListener listener) {
        if (spinnerStore != null && stores != null && !stores.isEmpty()) {
            android.widget.ArrayAdapter<Store> adapter =
                new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stores);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStore.setAdapter(adapter);
            spinnerStore.setOnItemSelectedListener(listener);
        }
    }
    
    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        
        // Navigasi hanya lewat navigationView (drawer)
    }
    
    private void setupUserInfo() {
        String userName = currentUser != null ? currentUser.getEmail() : "Guest";
        String userRole = (currentUser != null ? currentUser.getRole().equalsIgnoreCase("admin") : false) ? "Administrator" : "Cashier";
        
        tvUserName.setText(userName);
        tvUserRole.setText(userRole);
        
        Menu menu = navigationView.getMenu();
        if (!loginViewModel.isAdmin()) {
            MenuItem item;
            item = menu.findItem(R.id.nav_users); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_settings); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_dashboard); if (item != null) item.setVisible(false); // Hide dashboard for users
            // item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false); // Pengeluaran DITAMPILKAN untuk user
            item = menu.findItem(R.id.nav_report); if (item != null) item.setVisible(false); // Hide report for users
        } else {
            // Hide transaksi, riwayat, dan pengeluaran untuk admin
            MenuItem item;
            item = menu.findItem(R.id.nav_transaction); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_history); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false);
        }
        
        // Hide juga di bottom navigation
        if (loginViewModel.isAdmin()) {
            MenuItem item;
            item = menu.findItem(R.id.nav_transaction); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_history); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_expense); if (item != null) item.setVisible(false);
        } else {
            MenuItem item;
            item = menu.findItem(R.id.nav_dashboard); if (item != null) item.setVisible(false);
            item = menu.findItem(R.id.nav_report); if (item != null) item.setVisible(false);
            // Pengeluaran DITAMPILKAN untuk user
        }
        
        // Invalidate options menu to update toolbar items
        invalidateOptionsMenu();
    }
    
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        
        // Clear subtitle dan spinner toolbar secara default
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(null);
        }
        // clearToolbarStoreSpinner(); // Hapus karena toolbarStoreSpinner sudah dihapus
        
        // Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_dashboard) {
            // Only allow dashboard for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new DashboardFragment());
                getSupportActionBar().setTitle("Dashboard");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_products) {
            loadFragment(new ProdukFragment());
            getSupportActionBar().setTitle("Produk");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_transaction) {
            loadFragment(new TransaksiFragment());
            getSupportActionBar().setTitle("Transaksi");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_history) {
            loadFragment(new HistoryFragment());
            getSupportActionBar().setTitle("Riwayat");
            getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_expense) {
            // Only allow expense for admin
                loadFragment(new ExpenseFragment());
                getSupportActionBar().setTitle("Pengeluaran");
                getSupportActionBar().setSubtitle(null);
        } else if (itemId == R.id.nav_report) {
            // Only allow report for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new ReportFragment());
                getSupportActionBar().setTitle("Laporan");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_users) {
            // Only allow user management for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new UserManagementFragment());
                getSupportActionBar().setTitle("Manajemen User");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_settings) {
            // Only allow settings for admin
            if (loginViewModel.isAdmin()) {
                loadFragment(new SettingsFragment());
                getSupportActionBar().setTitle("Pengaturan");
                getSupportActionBar().setSubtitle(null);
            }
        } else if (itemId == R.id.nav_logout) {
            logout();
        }
        
        return true;
    }
    
    private void logout() {
        loginViewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    // HAPUS: onCreateOptionsMenu method
} 