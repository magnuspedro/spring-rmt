package br.com.magnus.config.starter.utils;

import br.com.magnus.config.starter.members.candidates.RefactoringCandidate;
import br.com.magnus.config.starter.members.detectors.methods.Reference;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class JsonUtils {

	public static JsonObject toJson(RefactoringCandidate c) {
		return Json.createObjectBuilder().add("id", c.getId()).add("reference", toJson(c.getReference()))
				.add("pkg", c.getPkg()).add("className", c.getClassName())
				.add("eligiblePattern", c.getEligiblePattern().name()).build();
	}

	public static JsonObject toJson(Reference r) {
		final JsonArrayBuilder authors = Json.createArrayBuilder();
		r.authors().forEach(authors::add);

		return Json.createObjectBuilder().add("title", r.title()).add("year", String.valueOf(r.year()))
				.add("authors", authors.build()).build();
	}

}