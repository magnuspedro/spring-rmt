package br.com.messages.utils;

import br.com.messages.members.candidates.RefactoringCandidate;
import br.com.messages.members.detectors.methods.Reference;

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
		r.getAuthors().forEach(authors::add);

		return Json.createObjectBuilder().add("title", r.getTitle()).add("year", String.valueOf(r.getYear()))
				.add("authors", authors.build()).build();
	}

}
