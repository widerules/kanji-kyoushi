package com.kanjikyoushi.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.kanjikyoushi.model.VocabularyWord;

@Path("vocabulary")
@RequestScoped
public interface VocabularyRepository {

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<VocabularyWord> getAll();

}
