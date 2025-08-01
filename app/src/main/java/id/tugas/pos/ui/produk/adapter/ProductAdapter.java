package id.tugas.pos.ui.produk.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import id.tugas.pos.R;
import id.tugas.pos.data.model.Product;
import id.tugas.pos.utils.CurrencyUtils;

public class ProductAdapter extends ListAdapter<Product, ProductAdapter.ProductViewHolder> {

    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onProductLongClick(Product product);
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName;
        private TextView tvProductCode;
        private TextView tvProductPrice;
        private TextView tvProductStock;
        private TextView tvProductCategory;
        private MaterialButton btnEdit;
        private MaterialButton btnDelete;
        private android.widget.ImageView ivProductImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCode = itemView.findViewById(R.id.tv_product_code);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvProductStock = itemView.findViewById(R.id.tv_product_stock);
            tvProductCategory = itemView.findViewById(R.id.tv_product_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);

            // Set click listeners untuk buttons
            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(getItem(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(getItem(position));
                }
            });

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductCode.setText(product.getCode());
            tvProductPrice.setText(CurrencyUtils.formatCurrency(product.getPrice()));
            tvProductStock.setText("Stok: " + product.getStock());
            tvProductCategory.setText(product.getCategory());
            
            // Load product image
            loadProductImage(product);
        }
        
        private void loadProductImage(Product product) {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                try {
                    java.io.File file = new java.io.File(product.getImagePath());
                    if (file.exists()) {
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(file.getAbsolutePath());
                        ivProductImage.setImageBitmap(bitmap);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Set default placeholder if no image or error
            ivProductImage.setImageResource(R.drawable.ic_product_placeholder);
        }
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getCode().equals(newItem.getCode()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   oldItem.getStock() == newItem.getStock() &&
                   oldItem.getCategory().equals(newItem.getCategory());
        }
    };
} 