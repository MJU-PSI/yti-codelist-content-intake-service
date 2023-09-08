-- annotation definition

CREATE TABLE annotation (
	id uuid NOT NULL,
	codevalue text NOT NULL,
	created timestamp NULL,
	modified timestamp NULL,
	CONSTRAINT annotation_pkey PRIMARY KEY (id),
	CONSTRAINT annotation_codevalue_key UNIQUE (codevalue)
);

-- annotation_preflabel definition

CREATE TABLE annotation_preflabel (
	annotation_id uuid NOT NULL,
	"language" text NOT NULL,
	preflabel text NOT NULL,
	CONSTRAINT annotation_preflabel_pkey PRIMARY KEY (annotation_id, language)
);

-- annotation_preflabel foreign keys

ALTER TABLE annotation_preflabel ADD CONSTRAINT fk_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(id) ON DELETE CASCADE;


-- annotation_description definition

CREATE TABLE annotation_description (
	annotation_id uuid NOT NULL,
	"language" text NOT NULL,
	description text NOT NULL,
	CONSTRAINT annotation_description_pkey PRIMARY KEY (annotation_id, language)
);

-- annotation_description foreign keys

ALTER TABLE annotation_description ADD CONSTRAINT fk_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(id) ON DELETE CASCADE;


-- codescheme_annotation definition

CREATE TABLE codescheme_annotation (
	codescheme_id uuid NOT NULL,
	annotation_id uuid NOT NULL,
	"language" text NOT NULL,
	"value" text NOT NULL,
	CONSTRAINT codescheme_annotation_pkey PRIMARY KEY (codescheme_id, annotation_id, "language")
);

-- codescheme_annotation foreign keys

ALTER TABLE codescheme_annotation ADD CONSTRAINT fk_codescheme_id FOREIGN KEY (codescheme_id) REFERENCES codescheme(id) ON DELETE CASCADE;
ALTER TABLE codescheme_annotation ADD CONSTRAINT fk_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(id) ON DELETE CASCADE;