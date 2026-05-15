CREATE OR REPLACE FUNCTION save_full_medical_record(
    p_ex_form_id bigint,
    p_chan_doan_chinh text,
    p_clinical_ids bigint[],
    p_medicines jsonb
) RETURNS void AS $$
DECLARE
    v_mr_id bigint;
    v_med jsonb;
BEGIN
    INSERT INTO medical_record (ex_form_id, chan_doan_chinh)
    VALUES (p_ex_form_id, p_chan_doan_chinh)
    RETURNING id INTO v_mr_id;

    IF p_clinical_ids IS NOT NULL AND array_length(p_clinical_ids, 1) > 0 THEN
        INSERT INTO medical_record_clinical (medical_record_id, clinical_id)
        SELECT v_mr_id, unnest(p_clinical_ids);
    END IF;

    IF p_medicines IS NOT NULL THEN
        FOR v_med IN SELECT * FROM jsonb_array_elements(p_medicines) LOOP
            INSERT INTO medical_record_medicine (
                medical_record_id,
                medicine_id,
                so_luong,
                lieu_dung,
                tan_suat,
                thoi_gian
            )
            VALUES (
                v_mr_id,
                (v_med->>'medicine_id')::bigint,
                COALESCE((v_med->>'so_luong')::int, 0),
                COALESCE(v_med->>'lieu_dung', ''),
                COALESCE(v_med->>'tan_suat', ''),
                COALESCE(v_med->>'thoi_gian', '')
            );
        END LOOP;
    END IF;
END;
$$ LANGUAGE plpgsql;
