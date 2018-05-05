package mk.ukim.finki.mpip.lab_rest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import mk.ukim.finki.mpip.lab_rest.R;
import mk.ukim.finki.mpip.lab_rest.models.MovieFull;
import mk.ukim.finki.mpip.lab_rest.rest.MovieApiRepository;
import mk.ukim.finki.mpip.lab_rest.rest.MovieApiRepositoryImpl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String MOVIE_KEY = "movie_key";

    private ProgressBar progressBar;
    private String imdbId;
    private MovieApiRepository api = new MovieApiRepositoryImpl();
    private Call<MovieFull> apiCall;
    private String movieUrl = "http://www.omdbapi.com/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        progressBar = findViewById(R.id.progressMovieDetails);
        getSupportActionBar().setTitle(R.string.movie_details);
        imdbId = getIntent().getStringExtra(MOVIE_KEY);
        if (imdbId != null && !imdbId.equals("")) {
            apiCall = api.getMovieById(imdbId);
            getMovie();
        } else {
            showError();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_share) {
            shareMovie();
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (apiCall != null && !apiCall.isCanceled()) {
            apiCall.cancel();
        }
        progressBar.setVisibility(View.GONE);
    }

    private void getMovie() {
        progressBar.setVisibility(View.VISIBLE);
        apiCall.enqueue(new Callback<MovieFull>() {
            @Override
            public void onResponse(Call<MovieFull> call, Response<MovieFull> response) {
                if (response.isSuccessful()) {
                    MovieFull movie = response.body();
                    if (movie != null) {
                        updateUi(movie);
                    }
                } else {
                    showError();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MovieFull> call, Throwable t) {
                if (!call.isCanceled()) {
                    showError();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateUi(MovieFull movie) {
        // title
        TextView txtTitle = findViewById(R.id.txtMovieTitleFull);
        txtTitle.setText(movie.getTitle());
        // year
        TextView txtYear = findViewById(R.id.txtMovieYearFull);
        txtYear.setText(movie.getYear());
        // runtime
        TextView txtRuntime = findViewById(R.id.txtMovieRuntimeFull);
        txtRuntime.setText(movie.getRuntime());
        // director
        TextView txtDirector = findViewById(R.id.txtMovieDirectorFull);
        txtDirector.setText(movie.getDirector());
        // writer
        TextView txtWriter = findViewById(R.id.txtMovieWriterFull);
        txtWriter.setText(movie.getWriter());
        // actors
        TextView txtActors = findViewById(R.id.txtMovieActorsFull);
        txtActors.setText(movie.getActors());
        // plot
        TextView txtPlot = findViewById(R.id.txtMoviePlotFull);
        txtPlot.setText(movie.getPlot());
        // poster
        ImageView imgMoviePoster = findViewById(R.id.imgMoviePosterLarge);
        Picasso.with(this)
                .load(movie.getPoster())
                .placeholder(R.drawable.ic_action_download)
                .into(imgMoviePoster);

        // website url
        movieUrl = movie.getWebsite();
    }

    private void shareMovie() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, movieUrl);
        Intent chooser = Intent.createChooser(intent, "Choose application");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "No adequate activity for this intent", Toast.LENGTH_LONG).show();
        }
    }

    private void showError() {
        Toast.makeText(this,
                R.string.error_api_call, Toast.LENGTH_LONG).show();
    }
}
