package com.elvsf.dragondeal.screens;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.api.UploadApi;
import com.elvsf.dragondeal.databinding.ActivityAddListingBinding;
import com.elvsf.dragondeal.dto.CreateListingRequest;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.dto.UploadImageResponse;
import com.elvsf.dragondeal.util.ApiCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

import static org.koin.java.KoinJavaComponent.get;

public class AddListingActivity extends AppCompatActivity {

    private ActivityAddListingBinding binding;

    private ListingApi listingApi;
    private UploadApi uploadApi;

    private Uri selectedImageUri;
    private String uploadedImageUrl;

    private final String[] currencies = {"USDT", "USDC"};
    private final String[] networks = {"POLYGON", "ETHEREUM", "BSC"};

    private final String[] games = {
            "CS2",
            "Dota 2",
            "Rust",
            "Valorant",
            "Fortnite",
            "Minecraft",
            "GTA V"
    };

    private final String[] categories = {
            "Аккаунты",
            "Скины",
            "Бустинг",
            "Донат",
            "Подписки",
            "Пополнение",
            "Предметы"
    };

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }

                selectedImageUri = uri;
                uploadedImageUrl = null;

                binding.imageProduct.setPadding(0, 0, 0, 0);
                binding.imageProduct.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                binding.imageProduct.clearColorFilter();
                binding.imageProduct.setImageURI(uri);

                binding.buttonChooseImage.setText("Картинка выбрана");
                uploadSelectedImage(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddListingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listingApi = get(ListingApi.class);
        uploadApi = get(UploadApi.class);

        setupSpinners();
        setupClicks();
    }

    private void setupSpinners() {
        binding.spinnerCurrency.setAdapter(createDarkSpinnerAdapter(currencies));
        binding.spinnerNetwork.setAdapter(createDarkSpinnerAdapter(networks));
        binding.spinnerGame.setAdapter(createDarkSpinnerAdapter(games));
        binding.spinnerCategory.setAdapter(createDarkSpinnerAdapter(categories));

        binding.spinnerGame.setSelection(2);
    }

    private ArrayAdapter<String> createDarkSpinnerAdapter(String[] values) {
        return new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                values
        ) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                styleSelectedSpinnerText(textView);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                styleDropdownSpinnerText(textView);
                return textView;
            }
        };
    }

    private void styleSelectedSpinnerText(TextView textView) {
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textView.setSingleLine(true);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setPadding(dp(16), 0, dp(16), 0);
    }

    private void styleDropdownSpinnerText(TextView textView) {
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textView.setBackgroundColor(Color.parseColor("#202020"));
        textView.setPadding(dp(18), dp(14), dp(18), dp(14));
    }

    private void setupClicks() {
        binding.buttonBackToMain.setOnClickListener(view -> finish());

        binding.buttonChooseImage.setOnClickListener(view ->
                imagePickerLauncher.launch("image/*")
        );

        binding.buttonPublish.setOnClickListener(view -> publishListing());
    }

    private void uploadSelectedImage(Uri imageUri) {
        try {
            setLoading(true);
            binding.buttonChooseImage.setText("Загружаем...");

            File imageFile = createTempFileFromUri(imageUri);

            String mimeType = getContentResolver().getType(imageUri);

            if (mimeType == null || mimeType.trim().isEmpty()) {
                mimeType = "image/jpeg";
            }

            RequestBody requestBody = RequestBody.create(
                    imageFile,
                    MediaType.parse(mimeType)
            );

            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "file",
                    imageFile.getName(),
                    requestBody
            );

            uploadApi.uploadListingImage(filePart).enqueue(new ApiCallback<UploadImageResponse>(this) {
                @Override
                protected void onSuccess(Response<UploadImageResponse> response) {
                    setLoading(false);
                    uploadedImageUrl = response.body().getImageUrl();
                    binding.buttonChooseImage.setText("Картинка загружена");

                    Toast.makeText(
                            AddListingActivity.this,
                            "Картинка загружена",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                protected void showError(String message) {
                    setLoading(false);
                    uploadedImageUrl = null;
                    binding.buttonChooseImage.setText("Выбрать изображение");
                    super.showError(message);
                }
            });

        } catch (Exception e) {
            setLoading(false);

            uploadedImageUrl = null;
            binding.buttonChooseImage.setText("Выбрать изображение");

            Toast.makeText(
                    this,
                    "Ошибка файла: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private File createTempFileFromUri(Uri uri) throws Exception {
        String fileName = getFileName(uri);

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "listing_image.jpg";
        }

        File tempFile = new File(getCacheDir(), fileName);

        try (
                InputStream inputStream = getContentResolver().openInputStream(uri);
                OutputStream outputStream = new FileOutputStream(tempFile)
        ) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Не удалось открыть файл");
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return tempFile;
    }

    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getLastPathSegment();
        }

        return result;
    }

    private void publishListing() {
        String title = binding.editTitle.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String priceText = binding.editPrice.getText().toString().trim();

        if (title.isEmpty()) {
            binding.editTitle.setError("Введите название");
            return;
        }

        if (description.isEmpty()) {
            binding.editDescription.setError("Введите описание");
            return;
        }

        if (priceText.isEmpty()) {
            binding.editPrice.setError("Введите цену");
            return;
        }

        BigDecimal price;

        try {
            price = new BigDecimal(priceText);
        } catch (Exception e) {
            binding.editPrice.setError("Некорректная цена");
            return;
        }

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            binding.editPrice.setError("Цена должна быть больше 0");
            return;
        }

        if (uploadedImageUrl == null || uploadedImageUrl.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Сначала выберите и загрузите картинку",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Long categoryId = getSelectedCategoryId();
        Long gameId = getSelectedGameId();

        String currency = binding.spinnerCurrency.getSelectedItem().toString();
        String network = binding.spinnerNetwork.getSelectedItem().toString();
        String deliveryType = "MANUAL";

        CreateListingRequest request = new CreateListingRequest(
                categoryId,
                gameId,
                title,
                description,
                price,
                currency,
                network,
                deliveryType,
                Collections.singletonList(uploadedImageUrl)
        );

        setLoading(true);

        listingApi.createListing(request).enqueue(new ApiCallback<ListingResponse>(this) {
            @Override
            protected void onSuccess(Response<ListingResponse> response) {
                setLoading(false);

                Toast.makeText(
                        AddListingActivity.this,
                        "Объявление опубликовано",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

            @Override
            protected void showError(String message) {
                setLoading(false);
                super.showError(message);
            }
        });
    }

    private Long getSelectedGameId() {
        String game = binding.spinnerGame.getSelectedItem().toString();

        switch (game) {
            case "CS2":
                return 1L;
            case "Dota 2":
                return 2L;
            case "Rust":
                return 3L;
            case "Valorant":
                return 4L;
            case "Fortnite":
                return 5L;
            case "Minecraft":
                return 6L;
            case "GTA V":
                return 7L;
            default:
                return 3L;
        }
    }

    private Long getSelectedCategoryId() {
        String category = binding.spinnerCategory.getSelectedItem().toString();

        switch (category) {
            case "Аккаунты":
                return 1L;
            case "Скины":
                return 3L;
            case "Бустинг":
                return 4L;
            case "Донат":
                return 2L;
            case "Подписки":
                return 5L;
            case "Пополнение":
                return 6L;
            case "Предметы":
                return 7L;
            default:
                return 1L;
        }
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        binding.buttonPublish.setEnabled(!loading);
        binding.buttonChooseImage.setEnabled(!loading);
        binding.spinnerCurrency.setEnabled(!loading);
        binding.spinnerNetwork.setEnabled(!loading);
        binding.spinnerGame.setEnabled(!loading);
        binding.spinnerCategory.setEnabled(!loading);

        if (loading) {
            binding.buttonPublish.setText("Подождите...");
        } else {
            binding.buttonPublish.setText("Опубликовать объявление");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
