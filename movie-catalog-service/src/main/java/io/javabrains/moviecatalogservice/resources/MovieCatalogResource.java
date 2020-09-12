package io.javabrains.moviecatalogservice.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.javabrains.moviecatalogservice.model.CatalogItem;
import io.javabrains.moviecatalogservice.model.Movie;
import io.javabrains.moviecatalogservice.model.Rating;
import io.javabrains.moviecatalogservice.model.UserRating;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private WebClient.Builder webClientBuilder;

	@RequestMapping("/{userId}")
	@HystrixCommand(fallbackMethod = "getFallbackCatalog")
	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId){
		
		//4. Move the creation of template to springAplication; inject/autowire it to use here or anywhere in application
		//RestTemplate restTemplate = new RestTemplate();
		//Movie movie = restTemplate.getForObject("http://localhost:8082/movies/foo", Movie.class);
		
		//5. using WebClient to make API call; but having instead here will create new instance with every call to getCatalog;
		//so move it to service ; create as a bean and inject it using autowired
		//WebClient.Builder builder = WebClient.builder(); //its builder pattern
				
		//1. get all rated movie ids (for this step harcoding)
		//7. we gonna remove this hardcoding and make an API call to get this data
//		List<Rating> ratings = Arrays.asList(
//				new Rating("1234", 4), 
//				new Rating("567", 3));
		
		UserRating ratings = getUserRatings(userId);
		
		
		//2. for each movie id, call movie info service and get details (in this step i am hardcoding it)
		//return ratings.stream().map(rating -> new CatalogItem("Transformers","Test", 4))
		//.collect(Collectors.toList());
		
		return ratings.getUserRating().stream().map(rating -> {
		return getCatalogItems(rating);
		})
		.collect(Collectors.toList());
		
		
		//6. commenting above step and using webclient to make API call
//		return ratings.stream().map(rating -> {
//		Movie movie = webClientBuilder.build()
//				.get()
//				.uri("http://localhost:8082/movies/" + rating.getMovieId())
//				.retrieve()
//				.bodyToMono(Movie.class)
//				.block();
//		return new CatalogItem(movie.getName(), "Test desc ", rating.getRating());
//		})
//		.collect(Collectors.toList());
		
		
		
		//3. put them all together
		
//		return Collections.singletonList(
//				new CatalogItem("Transformers","Test", 4)
//				);
		
		
	}

	private CatalogItem getCatalogItems(Rating rating) {
		Movie movie = restTemplate.getForObject("http://movie-info-service/movies/" + rating.getMovieId(), Movie.class);
		return new CatalogItem(movie.getName(), "Test desc ", rating.getRating());
	}

	private UserRating getUserRatings(String userId) {
		return restTemplate.getForObject("http://rating-data-service/ratingsData/users/" + userId, UserRating.class);
	}
	
	
	public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId){
		
		return Arrays.asList(new CatalogItem("No Name", "",0));
		
	}
	
	
}
