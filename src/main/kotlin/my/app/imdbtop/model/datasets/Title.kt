package my.app.imdbtop.model.datasets

import com.fasterxml.jackson.annotation.JsonCreator

data class Title  @JsonCreator constructor(
    val titleId: String,          // a tConst, an alphanumeric unique identifier of the title
    val ordering: Int,            // a number to uniquely identify rows for a given titleId
    val title: String,            // the localized title
    val region: String,           // the region for this version of the title
    val language: String,         // the language of the title
    val types: List<String>,      // Enumerated set of attributes for this alternative title. One or more of the following: "alternative", "dvd", "festival", "tv", "video", "working", "original", "imdbDisplay". New values may be added in the future without warning
    val attributes: List<String>, // Additional terms to describe this alternative title, not enumerated
    val isOriginalTitle: Boolean, // 0: not original title; 1: original title
): DataSet