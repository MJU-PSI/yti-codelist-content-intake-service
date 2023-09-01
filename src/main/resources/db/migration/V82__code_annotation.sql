-- code_annotation definition

CREATE TABLE code_annotation (
	code_id uuid NOT NULL,
	annotation_id uuid NOT NULL,
	"language" text NOT NULL,
	"value" text NOT NULL,
	CONSTRAINT code_annotation_pkey PRIMARY KEY (code_id, annotation_id, "language")
);

-- code_annotation foreign keys

ALTER TABLE code_annotation ADD CONSTRAINT fk_code_id FOREIGN KEY (code_id) REFERENCES code(id) ON DELETE CASCADE;
ALTER TABLE code_annotation ADD CONSTRAINT fk_annotation_id FOREIGN KEY (annotation_id) REFERENCES annotation(id) ON DELETE CASCADE;