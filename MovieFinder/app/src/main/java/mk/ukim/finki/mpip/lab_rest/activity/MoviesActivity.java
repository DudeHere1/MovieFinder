package mk.ukim.finki.mpip.lab_rest.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mk.ukim.finki.mpip.lab_rest.R;
import mk.ukim.finki.mpip.lab_rest.adapter.MovieAdapter;
import mk.ukim.finki.mpip.lab_rest.models.MovieShort;
import mk.ukim.finki.mpip.lab_rest.models.Search;
import mk.ukim.finki.mpip.lab_rest.rest.MovieApiRepository;
import mk.ukim.finki.mpip.lab_rest.rest.MovieApiRepositoryImpl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieClickListener {

    private static final String MOVIE_LIST_KEY = "movie_list_key";
    private static final String MOVIE_KEY = "movie_key";

    private EditText editTextSearch;
    private ImageView imgSearch;
    private MovieApiRepository api = new MovieApiRepositoryImpl();
    private Call<Search> apiCall;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        bindUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (apiCall != null && !apiCall.isCanceled()) {
            apiCall.cancel();
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_LIST_KEY, adapter.getMovies());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<MovieShort> movies = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
        if (movies != null) {
            adapter.clear();
            adapter.init(movies);
        }
    }

    @Override
    public void onMovieClick(String imdbId) {
        Intent intent = new Intent(this, MovieDetailsActivity.class);
        intent.putExtra(MOVIE_KEY, imdbId);
        startActivity(intent);
    }

    @Override
    public void onLongMovieClick(String imdbId) {
        List<MovieShort> movies = adapter.getMovies();
        for (int i = 0; i < movies.size(); ++i) {
            if (imdbId.equals(movies.get(i).getImdbID())) {
                adapter.removeMovie(i);
                break;
            }
        }
    }

    private void bindUi() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editTextSearch = findViewById(R.id.editSearch);
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    attemptSearch();
                }
                return true;
            }
        });
        imgSearch = findViewById(R.id.imgSearch);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSearch();
            }
        });
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.recyclerMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovieAdapter(new ArrayList<MovieShort>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void search(String name) {
        apiCall = api.searchMovies(name);
        progressBar.setVisibility(View.VISIBLE);
        apiCall.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(Call<Search> call, Response<Search> response) {
                if (response.isSuccessful()) {
                    Search search = response.body();
                    if (search != null && search.getSearch() != null) {
                        adapter.clear();
                        adapter.init(search.getSearch());
                    } else {
                        showError();
                    }
                } else {
                    showError();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Search> call, Throwable t) {
                if (!call.isCanceled()) {
                    progressBar.setVisibility(View.GONE);
                    showError();
                }
            }
        });
    }

    private void showError() {
        Toast.makeText(this,
                R.string.error_api_call, Toast.LENGTH_LONG).show();
    }

    private void attemptSearch() {
        if (!isQueryEmpty()) {
            hideSoftKeyboard();
            search(editTextSearch.getText().toString().trim());
        } else {
            Toast.makeText(MoviesActivity.this,
                    R.string.error_empty_query, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isQueryEmpty() {
        return editTextSearch.getText().toString().trim().equals("");
    }

    private void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

}
