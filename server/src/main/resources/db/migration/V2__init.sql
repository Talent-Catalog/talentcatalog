-- Extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS vector;


--
-- PostgreSQL database
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


--
-- Name: bump_candidate_on_user_change(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_on_user_change() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    update candidate
    set data_version = data_version + 1
    where user_id = coalesce(new.id, old.id);

    return null;
end;
$$;


--
-- Name: bump_candidate_ref_version(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_ref_version() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    update candidate
    set data_version = data_version + 1
    where id = coalesce(new.candidate_id, old.candidate_id);

    return null;
end;
$$;


--
-- Name: bump_candidate_self_version(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.bump_candidate_self_version() RETURNS trigger
    LANGUAGE plpgsql
AS $$
begin
    -- Note that this logic is different from updates on other tables because updating fields on the
    -- candidate table will change the data_version field on the candidate table, which will trigger
    -- another update - and so on, resulting in an infinite loop.
    -- Only bump version if something other than data_version changed
    if new is distinct from old then
        new.data_version := old.data_version + 1;
    end if;

    -- Return NEW to apply the modified row
    return new;
end;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.audit_log (
                                  id bigint NOT NULL,
                                  event_date date NOT NULL,
                                  user_id bigint NOT NULL,
                                  type text NOT NULL,
                                  action text NOT NULL,
                                  object_ref text NOT NULL,
                                  description text NOT NULL
);


--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- Name: batch_job_execution; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution (
                                            job_execution_id bigint NOT NULL,
                                            version bigint,
                                            job_instance_id bigint NOT NULL,
                                            create_time timestamp without time zone NOT NULL,
                                            start_time timestamp without time zone,
                                            end_time timestamp without time zone,
                                            status character varying(10),
                                            exit_code character varying(2500),
                                            exit_message character varying(2500),
                                            last_updated timestamp without time zone
);


--
-- Name: batch_job_execution_context; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution_context (
                                                    job_execution_id bigint NOT NULL,
                                                    short_context character varying(2500) NOT NULL,
                                                    serialized_context text
);


--
-- Name: batch_job_execution_params; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_execution_params (
                                                   job_execution_id bigint NOT NULL,
                                                   parameter_name character varying(100) NOT NULL,
                                                   parameter_type character varying(100) NOT NULL,
                                                   parameter_value character varying(2500),
                                                   identifying character(1) NOT NULL
);


--
-- Name: batch_job_execution_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_job_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_job_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_job_instance (
                                           job_instance_id bigint NOT NULL,
                                           version bigint,
                                           job_name character varying(100) NOT NULL,
                                           job_key character varying(32) NOT NULL
);


--
-- Name: batch_job_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_job_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: batch_step_execution; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_step_execution (
                                             step_execution_id bigint NOT NULL,
                                             version bigint NOT NULL,
                                             step_name character varying(100) NOT NULL,
                                             job_execution_id bigint NOT NULL,
                                             create_time timestamp without time zone NOT NULL,
                                             start_time timestamp without time zone,
                                             end_time timestamp without time zone,
                                             status character varying(10),
                                             commit_count bigint,
                                             read_count bigint,
                                             filter_count bigint,
                                             write_count bigint,
                                             read_skip_count bigint,
                                             write_skip_count bigint,
                                             process_skip_count bigint,
                                             rollback_count bigint,
                                             exit_code character varying(2500),
                                             exit_message character varying(2500),
                                             last_updated timestamp without time zone
);


--
-- Name: batch_step_execution_context; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.batch_step_execution_context (
                                                     step_execution_id bigint NOT NULL,
                                                     short_context character varying(2500) NOT NULL,
                                                     serialized_context text
);


--
-- Name: batch_step_execution_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.batch_step_execution_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate (
                                  id bigint NOT NULL,
                                  candidate_number character varying(16),
                                  user_id bigint,
                                  gender text,
                                  dob date,
                                  phone text,
                                  whatsapp text,
                                  status text NOT NULL,
                                  country_id bigint,
                                  city text,
                                  year_of_arrival integer,
                                  nationalityold_id bigint,
                                  unhcr_status text,
                                  unhcr_number text,
                                  additional_info text,
                                  max_education_level_id bigint,
                                  created_by bigint NOT NULL,
                                  created_date timestamp with time zone NOT NULL,
                                  updated_by bigint,
                                  updated_date timestamp with time zone,
                                  address1 text,
                                  candidate_message text,
                                  preferred_language text,
                                  migration_status text,
                                  sflink text,
                                  folderlink text,
                                  videolink text,
                                  survey_type_id bigint,
                                  survey_comment text,
                                  text_search_id text,
                                  returned_home text,
                                  returned_home_reason text,
                                  visa_issues text,
                                  visa_issues_notes text,
                                  avail_immediate text,
                                  avail_immediate_reason text,
                                  avail_immediate_notes text,
                                  family_move text,
                                  family_move_notes text,
                                  int_recruit_reasons text,
                                  int_recruit_rural text,
                                  return_home_safe text,
                                  work_permit text,
                                  work_permit_desired text,
                                  work_desired text,
                                  asylum_year date,
                                  home_location text,
                                  unhcr_not_reg_status text,
                                  unhcr_file integer,
                                  unhcr_notes text,
                                  unhcr_permission text,
                                  unrwa_number text,
                                  unrwa_notes text,
                                  dest_limit text,
                                  dest_limit_notes text,
                                  crime_convict text,
                                  crime_convict_notes text,
                                  conflict text,
                                  conflict_notes text,
                                  residence_status text,
                                  work_abroad text,
                                  host_entry_legally text,
                                  left_home_notes text,
                                  return_home_future text,
                                  return_home_when text,
                                  resettle_third text,
                                  resettle_third_status text,
                                  host_challenges text,
                                  marital_status text,
                                  partner_registered text,
                                  partner_candidate_id bigint,
                                  partner_edu_level_id bigint,
                                  partner_occupation_id bigint,
                                  partner_english text,
                                  partner_english_level_id bigint,
                                  partner_ielts text,
                                  partner_ielts_score text,
                                  partner_citizenship text,
                                  military_service text,
                                  visa_reject text,
                                  can_drive text,
                                  driving_license text,
                                  driving_license_exp date,
                                  driving_license_country_id bigint,
                                  host_entry_year integer,
                                  english_assessment text,
                                  english_assessment_score_ielts text,
                                  int_recruit_rural_notes text,
                                  work_abroad_notes text,
                                  host_entry_legally_notes text,
                                  visa_reject_notes text,
                                  partner_ielts_yr integer,
                                  partner_edu_level_notes text,
                                  partner_occupation_notes text,
                                  returned_home_reason_no text,
                                  residence_status_notes text,
                                  work_desired_notes text,
                                  left_home_reasons text,
                                  military_wanted text,
                                  military_notes text,
                                  military_start date,
                                  military_end date,
                                  int_recruit_other text,
                                  avail_immediate_job_ops text,
                                  unhcr_registered text,
                                  unrwa_registered text,
                                  birth_country_id bigint,
                                  linked_in_link text,
                                  marital_status_notes text,
                                  work_permit_desired_notes text,
                                  host_entry_year_notes text,
                                  unrwa_file integer,
                                  unrwa_not_reg_status text,
                                  ielts_score numeric,
                                  nationality_id bigint,
                                  health_issues text,
                                  health_issues_notes text,
                                  unhcr_consent text,
                                  shareable_notes text,
                                  shareable_cv_attachment_id bigint,
                                  shareable_doc_attachment_id bigint,
                                  mini_intake date,
                                  full_intake date,
                                  state text,
                                  external_id text,
                                  external_id_source text,
                                  covid_vaccinated text,
                                  covid_vaccinated_status text,
                                  covid_vaccinated_date date,
                                  covid_vaccine_name text,
                                  covid_vaccine_notes text,
                                  folderlink_address text,
                                  folderlink_character text,
                                  folderlink_employer text,
                                  folderlink_identity text,
                                  folderlink_medical text,
                                  folderlink_qualification text,
                                  folderlink_registration text,
                                  media_willingness text,
                                  folderlink_engagement text,
                                  folderlink_experience text,
                                  folderlink_family text,
                                  folderlink_immigration text,
                                  folderlink_language text,
                                  partner_ref text,
                                  rego_ip text,
                                  rego_utm_campaign text,
                                  rego_utm_content text,
                                  rego_utm_medium text,
                                  rego_utm_source text,
                                  rego_utm_term text,
                                  rego_partner_param text,
                                  rego_referrer_param text,
                                  contact_consent_registration boolean DEFAULT false NOT NULL,
                                  contact_consent_partners boolean DEFAULT false NOT NULL,
                                  mini_intake_completed_by bigint,
                                  mini_intake_completed_date timestamp with time zone,
                                  full_intake_completed_by bigint,
                                  full_intake_completed_date timestamp with time zone,
                                  french_assessment text,
                                  french_assessment_score_nclc integer,
                                  arrest_imprison text,
                                  arrest_imprison_notes text,
                                  avail_date date,
                                  potential_duplicate boolean DEFAULT false NOT NULL,
                                  public_id character varying(22),
                                  relocated_address text,
                                  relocated_city text,
                                  relocated_state text,
                                  relocated_country_id bigint,
                                  english_assessment_score_det integer,
                                  muted boolean DEFAULT false NOT NULL,
                                  all_notifications boolean DEFAULT false NOT NULL,
                                  registered_by bigint,
                                  change_password boolean DEFAULT false,
                                  accepted_privacy_policy_id text,
                                  accepted_privacy_policy_date timestamp with time zone,
                                  accepted_privacy_policy_partner_id bigint,
                                  text text,
                                  ts_text tsvector GENERATED ALWAYS AS (to_tsvector('english'::regconfig, text)) STORED,
                                  data_version bigint DEFAULT 0 NOT NULL
);


--
-- Name: candidate_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_attachment (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             type text,
                                             name text,
                                             created_by bigint,
                                             created_date timestamp with time zone,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             location text NOT NULL,
                                             migrated boolean DEFAULT false NOT NULL,
                                             file_type text,
                                             text_extract text,
                                             cv boolean DEFAULT false NOT NULL,
                                             upload_type text
);


--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_attachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_attachment_id_seq OWNED BY public.candidate_attachment.id;


--
-- Name: candidate_certification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_certification (
                                                id bigint NOT NULL,
                                                candidate_id bigint NOT NULL,
                                                name text,
                                                institution text,
                                                date_completed date
);


--
-- Name: candidate_certification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_certification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_certification_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_certification_id_seq OWNED BY public.candidate_certification.id;


--
-- Name: candidate_citizenship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_citizenship (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              nationality_id bigint,
                                              has_passport text,
                                              notes text,
                                              passport_exp date
);


--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_citizenship_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_citizenship_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_citizenship_id_seq OWNED BY public.candidate_citizenship.id;


--
-- Name: candidate_coupon_code; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_coupon_code (
                                              id bigint NOT NULL,
                                              offer_to_assist_id bigint,
                                              candidate_id bigint NOT NULL,
                                              coupon_code text
);


--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_coupon_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_coupon_code_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_coupon_code_id_seq OWNED BY public.candidate_coupon_code.id;


--
-- Name: candidate_dependant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_dependant (
                                            id bigint NOT NULL,
                                            candidate_id bigint NOT NULL,
                                            relation text,
                                            dob date,
                                            health_concern text,
                                            health_notes text,
                                            name text,
                                            registered text,
                                            relation_other text,
                                            registered_number text,
                                            registered_notes text,
                                            gender text
);


--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_dependant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_dependant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_dependant_id_seq OWNED BY public.candidate_dependant.id;


--
-- Name: candidate_destination; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_destination (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              country_id bigint,
                                              interest text,
                                              family text,
                                              location text,
                                              notes text
);


--
-- Name: candidate_destination_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_destination_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_destination_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_destination_id_seq OWNED BY public.candidate_destination.id;


--
-- Name: candidate_education; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_education (
                                            id bigint NOT NULL,
                                            candidate_id bigint NOT NULL,
                                            education_type text,
                                            country_id bigint NOT NULL,
                                            length_of_course_years integer,
                                            institution text,
                                            course_name text,
                                            year_completed integer,
                                            major_id bigint,
                                            incomplete boolean
);


--
-- Name: candidate_education_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_education_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_education_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_education_id_seq OWNED BY public.candidate_education.id;


--
-- Name: candidate_exam; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_exam (
                                       id bigint NOT NULL,
                                       candidate_id bigint NOT NULL,
                                       exam text,
                                       other_exam text,
                                       score text,
                                       year integer,
                                       notes text
);


--
-- Name: candidate_exam_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_exam_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_exam_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_exam_id_seq OWNED BY public.candidate_exam.id;


--
-- Name: candidate_form; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_form (
                                       id bigint NOT NULL,
                                       name text,
                                       description text
);


--
-- Name: candidate_form_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_form_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_form_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_form_id_seq OWNED BY public.candidate_form.id;


--
-- Name: candidate_form_instance; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_form_instance (
                                                candidate_id bigint NOT NULL,
                                                form_id bigint NOT NULL,
                                                created_date timestamp with time zone NOT NULL,
                                                updated_date timestamp with time zone
);


--
-- Name: candidate_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_id_seq OWNED BY public.candidate.id;


--
-- Name: candidate_job_experience; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_job_experience (
                                                 id bigint NOT NULL,
                                                 candidate_id bigint NOT NULL,
                                                 candidate_occupation_id bigint NOT NULL,
                                                 company_name text,
                                                 country_id bigint,
                                                 role text,
                                                 start_date date,
                                                 end_date date,
                                                 full_time boolean,
                                                 paid boolean,
                                                 description text,
                                                 ts tsvector GENERATED ALWAYS AS (to_tsvector('english'::regconfig, description)) STORED
);


--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_job_experience_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_job_experience_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_job_experience_id_seq OWNED BY public.candidate_job_experience.id;


--
-- Name: candidate_json_cache; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_json_cache (
                                             candidate_id bigint NOT NULL,
                                             data_version bigint NOT NULL,
                                             "json" jsonb NOT NULL,
                                             computed_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: candidate_language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_language (
                                           id bigint NOT NULL,
                                           candidate_id bigint NOT NULL,
                                           language_id bigint,
                                           written_level_id bigint,
                                           spoken_level_id bigint
);


--
-- Name: candidate_language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_language_id_seq OWNED BY public.candidate_language.id;


--
-- Name: candidate_note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_note (
                                       id bigint NOT NULL,
                                       candidate_id bigint NOT NULL,
                                       note_type text NOT NULL,
                                       title text NOT NULL,
                                       comment text,
                                       created_by bigint,
                                       created_date timestamp with time zone,
                                       updated_by bigint,
                                       updated_date timestamp with time zone
);


--
-- Name: candidate_note_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_note_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_note_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_note_id_seq OWNED BY public.candidate_note.id;


--
-- Name: candidate_occupation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_occupation (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             occupation_id bigint,
                                             years_experience integer NOT NULL,
                                             top_candidate boolean,
                                             migration_occupation text,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             created_by bigint,
                                             created_date timestamp with time zone
);


--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_occupation_id_seq OWNED BY public.candidate_occupation.id;


--
-- Name: candidate_opportunity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_opportunity (
                                              id bigint NOT NULL,
                                              candidate_id bigint,
                                              closing_comments_for_candidate text,
                                              employer_feedback text,
                                              job_opp_id bigint,
                                              stage text,
                                              closing_comments text,
                                              closed boolean DEFAULT false NOT NULL,
                                              name text,
                                              next_step text,
                                              next_step_due_date date,
                                              sf_id text,
                                              stage_order integer,
                                              created_by bigint,
                                              created_date timestamp with time zone,
                                              updated_by bigint,
                                              updated_date timestamp with time zone,
                                              won boolean DEFAULT false NOT NULL,
                                              file_offer_link text,
                                              file_offer_name text,
                                              last_active_stage text DEFAULT 'prospect'::text,
                                              relocating_dependant_ids text
);


--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_opportunity_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_opportunity_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_opportunity_id_seq OWNED BY public.candidate_opportunity.id;


--
-- Name: candidate_property; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_property (
                                           candidate_id bigint NOT NULL,
                                           name text NOT NULL,
                                           value text,
                                           related_task_assignment_id bigint
);


--
-- Name: candidate_property_definition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_property_definition (
                                                      id bigint NOT NULL,
                                                      name text NOT NULL,
                                                      label text,
                                                      definition text,
                                                      type text
);


--
-- Name: candidate_property_definition_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_property_definition_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_property_definition_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_property_definition_id_seq OWNED BY public.candidate_property_definition.id;


--
-- Name: candidate_review_item; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_review_item (
                                              id bigint NOT NULL,
                                              candidate_id bigint NOT NULL,
                                              saved_search_id bigint NOT NULL,
                                              review_status text NOT NULL,
                                              comment text,
                                              created_by bigint,
                                              created_date timestamp with time zone,
                                              updated_by bigint,
                                              updated_date timestamp with time zone
);


--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_review_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_review_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_review_item_id_seq OWNED BY public.candidate_review_item.id;


--
-- Name: candidate_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_saved_list (
                                             candidate_id bigint NOT NULL,
                                             saved_list_id bigint NOT NULL,
                                             context_note text,
                                             shareable_cv_attachment_id bigint,
                                             shareable_doc_attachment_id bigint
);


--
-- Name: candidate_skill; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_skill (
                                        id bigint NOT NULL,
                                        candidate_id bigint NOT NULL,
                                        skill text,
                                        time_period character varying(100)
);


--
-- Name: candidate_skill_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_skill_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_skill_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_skill_id_seq OWNED BY public.candidate_skill.id;


--
-- Name: candidate_visa_check; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_visa_check (
                                             id bigint NOT NULL,
                                             candidate_id bigint NOT NULL,
                                             country_id bigint,
                                             created_by bigint,
                                             created_date timestamp with time zone,
                                             updated_by bigint,
                                             updated_date timestamp with time zone,
                                             protection text,
                                             protection_grounds text,
                                             health_assessment text,
                                             health_assessment_notes text,
                                             character_assessment text,
                                             character_assessment_notes text,
                                             security_risk text,
                                             security_risk_notes text,
                                             valid_travel_docs text,
                                             valid_travel_docs_notes text,
                                             overall_risk text,
                                             overall_risk_notes text,
                                             english_threshold text,
                                             english_threshold_notes text,
                                             pathway_assessment text,
                                             pathway_assessment_notes text,
                                             destination_family text,
                                             destination_family_location text
);


--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_visa_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_visa_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_visa_check_id_seq OWNED BY public.candidate_visa_check.id;


--
-- Name: candidate_visa_job_check; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidate_visa_job_check (
                                                 id bigint NOT NULL,
                                                 interest text,
                                                 interest_notes text,
                                                 occupation_id bigint,
                                                 occupation_notes text,
                                                 salary_tsmit text,
                                                 regional text,
                                                 eligible_494 text,
                                                 eligible_494_notes text,
                                                 eligible_186 text,
                                                 eligible_186_notes text,
                                                 eligible_other text,
                                                 eligible_other_notes text,
                                                 put_forward text,
                                                 notes text,
                                                 candidate_visa_check_id bigint,
                                                 tbb_eligibility text,
                                                 qualification text,
                                                 qualification_notes text,
                                                 job_opp_id bigint,
                                                 relevant_work_exp text,
                                                 age_requirement text,
                                                 preferred_pathways text,
                                                 ineligible_pathways text,
                                                 eligible_pathways text,
                                                 occupation_category text,
                                                 occupation_sub_category text,
                                                 english_threshold text,
                                                 languages_threshold_notes text,
                                                 languages_required text,
                                                 languages_threshold_met text,
                                                 candidate_id bigint NOT NULL
);


--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidate_visa_job_check_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: candidate_visa_job_check_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidate_visa_job_check_id_seq OWNED BY public.candidate_visa_job_check.id;


--
-- Name: chat_post; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_post (
                                  id bigint NOT NULL,
                                  content text,
                                  job_chat_id bigint NOT NULL,
                                  created_by bigint NOT NULL,
                                  created_date timestamp with time zone NOT NULL,
                                  updated_by bigint,
                                  updated_date timestamp with time zone
);


--
-- Name: chat_post_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chat_post_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: chat_post_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.chat_post_id_seq OWNED BY public.chat_post.id;


--
-- Name: chatbot_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chatbot_message (
                                        id uuid NOT NULL,
                                        session_id uuid NOT NULL,
                                        question_id uuid NOT NULL,
                                        sender character varying(255) NOT NULL,
                                        message text NOT NULL,
                                        "timestamp" timestamp with time zone NOT NULL,
                                        referenced_faq_ids jsonb,
                                        CONSTRAINT chatbot_message_sender_check CHECK (((sender)::text = ANY ((ARRAY['USER'::character varying, 'BOT'::character varying])::text[])))
);


--
-- Name: COLUMN chatbot_message.referenced_faq_ids; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.chatbot_message.referenced_faq_ids IS 'Array of FAQ IDs that were referenced in generating this bot response. Used for tracking which FAQs are most commonly used.';


--
-- Name: country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country (
                                id bigint NOT NULL,
                                name text NOT NULL,
                                status text DEFAULT 'active'::text NOT NULL,
                                iso_code text
);


--
-- Name: country_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.country_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: country_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.country_id_seq OWNED BY public.country.id;


--
-- Name: country_nationality_join; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.country_nationality_join (
                                                 country_id bigint NOT NULL,
                                                 nationality_id bigint NOT NULL
);


--
-- Name: duolingo_coupon; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.duolingo_coupon (
                                        id bigint NOT NULL,
                                        coupon_code character varying(255) NOT NULL,
                                        candidate_id bigint,
                                        expiration_date timestamp with time zone NOT NULL,
                                        date_sent timestamp with time zone,
                                        coupon_status character varying(50) NOT NULL,
                                        test_type character varying(20)
);


--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.duolingo_coupon_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: duolingo_coupon_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.duolingo_coupon_id_seq OWNED BY public.duolingo_coupon.id;


--
-- Name: duolingo_extra_fields; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.duolingo_extra_fields (
                                              id bigint NOT NULL,
                                              certificate_url character varying(255),
                                              interview_url character varying(255),
                                              verification_date character varying(255),
                                              percent_score integer NOT NULL,
                                              scale integer NOT NULL,
                                              literacy_subscore integer NOT NULL,
                                              conversation_subscore integer NOT NULL,
                                              comprehension_subscore integer NOT NULL,
                                              production_subscore integer NOT NULL,
                                              candidate_exam_id bigint NOT NULL
);


--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.duolingo_extra_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: duolingo_extra_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.duolingo_extra_fields_id_seq OWNED BY public.duolingo_extra_fields.id;


--
-- Name: education_level; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.education_level (
                                        id bigint NOT NULL,
                                        name text NOT NULL,
                                        level integer NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL,
                                        education_type text,
                                        isced_code character varying(20)
);


--
-- Name: education_level_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.education_level_id_seq OWNED BY public.education_level.id;


--
-- Name: education_major; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.education_major (
                                        id bigint NOT NULL,
                                        name text NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: education_major_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.education_major_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: education_major_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.education_major_id_seq OWNED BY public.education_major.id;


--
-- Name: employer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.employer (
                                 id bigint NOT NULL,
                                 country_id bigint,
                                 description text,
                                 has_hired_internationally boolean,
                                 name text,
                                 sf_id text,
                                 website text,
                                 created_by bigint,
                                 created_date timestamp with time zone,
                                 updated_by bigint,
                                 updated_date timestamp with time zone
);


--
-- Name: employer_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.employer_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: employer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.employer_id_seq OWNED BY public.employer.id;


--
-- Name: export_column; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.export_column (
                                      id bigint NOT NULL,
                                      saved_list_id bigint,
                                      saved_search_id bigint,
                                      index integer NOT NULL,
                                      key text,
                                      properties text
);


--
-- Name: export_column_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.export_column_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: export_column_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.export_column_id_seq OWNED BY public.export_column.id;


--
-- Name: help_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.help_link (
                                  id bigint NOT NULL,
                                  country_id bigint,
                                  case_stage text,
                                  job_stage text,
                                  label text NOT NULL,
                                  link text NOT NULL,
                                  focus text,
                                  next_step_name text,
                                  next_step_text text,
                                  next_step_days integer,
                                  created_by bigint,
                                  created_date timestamp with time zone,
                                  updated_by bigint,
                                  updated_date timestamp with time zone
);


--
-- Name: help_link_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.help_link_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: help_link_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.help_link_id_seq OWNED BY public.help_link.id;


--
-- Name: industry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.industry (
                                 id bigint NOT NULL,
                                 name text NOT NULL,
                                 status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: industry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.industry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: industry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.industry_id_seq OWNED BY public.industry.id;


--
-- Name: job_chat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_chat (
                                 id bigint NOT NULL,
                                 created_by bigint NOT NULL,
                                 created_date timestamp with time zone NOT NULL,
                                 job_id bigint,
                                 updated_by bigint,
                                 updated_date timestamp with time zone,
                                 type text,
                                 source_partner_id bigint,
                                 candidate_id bigint
);


--
-- Name: job_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.job_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: job_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.job_chat_id_seq OWNED BY public.job_chat.id;


--
-- Name: job_chat_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_chat_user (
                                      job_chat_id bigint NOT NULL,
                                      user_id bigint NOT NULL,
                                      last_read_post_id bigint
);


--
-- Name: job_opp_intake; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_opp_intake (
                                       id bigint NOT NULL,
                                       recruitment_process text,
                                       employer_cost_commitment text,
                                       location text,
                                       location_details text,
                                       salary_range text,
                                       benefits text,
                                       language_requirements text,
                                       employment_experience text,
                                       education_requirements text,
                                       skill_requirements text,
                                       occupation_code text,
                                       min_salary text,
                                       visa_pathways text
);


--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.job_opp_intake_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: job_opp_intake_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.job_opp_intake_id_seq OWNED BY public.job_opp_intake.id;


--
-- Name: job_suggested_saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.job_suggested_saved_search (
                                                   tc_job_id bigint NOT NULL,
                                                   saved_search_id bigint NOT NULL
);


--
-- Name: language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language (
                                 id bigint NOT NULL,
                                 name text NOT NULL,
                                 status text DEFAULT 'active'::text NOT NULL,
                                 iso_code text
);


--
-- Name: language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.language_id_seq OWNED BY public.language.id;


--
-- Name: language_level; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.language_level (
                                       id bigint NOT NULL,
                                       name text NOT NULL,
                                       status text DEFAULT 'active'::text NOT NULL,
                                       level integer NOT NULL,
                                       cefr_level character varying(20)
);


--
-- Name: language_level_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.language_level_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: language_level_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.language_level_id_seq OWNED BY public.language_level.id;


--
-- Name: link_preview; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.link_preview (
                                     id bigint NOT NULL,
                                     chat_post_id bigint,
                                     url text,
                                     title text,
                                     description text,
                                     image_url text,
                                     domain text,
                                     favicon_url text
);


--
-- Name: link_preview_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.link_preview_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: link_preview_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.link_preview_id_seq OWNED BY public.link_preview.id;


--
-- Name: nationality; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nationality (
                                    id bigint NOT NULL,
                                    name text NOT NULL,
                                    status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: nationality_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.nationality_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: nationality_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.nationality_id_seq OWNED BY public.nationality.id;


--
-- Name: occupation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occupation (
                                   id bigint NOT NULL,
                                   name text NOT NULL,
                                   status text DEFAULT 'active'::text NOT NULL,
                                   isco08_code text
);


--
-- Name: occupation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: occupation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.occupation_id_seq OWNED BY public.occupation.id;


--
-- Name: offer_to_assist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.offer_to_assist (
                                        id bigint NOT NULL,
                                        created_by bigint NOT NULL,
                                        created_date timestamp with time zone NOT NULL,
                                        updated_by bigint,
                                        updated_date timestamp with time zone,
                                        additional_notes text,
                                        partner_id bigint NOT NULL,
                                        public_id character varying(22),
                                        reason text
);


--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.offer_to_assist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: offer_to_assist_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.offer_to_assist_id_seq OWNED BY public.offer_to_assist.id;


--
-- Name: partner; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner (
                                default_source_partner boolean DEFAULT false NOT NULL,
                                id bigint NOT NULL,
                                logo text,
                                name text NOT NULL,
                                status text NOT NULL,
                                registration_landing_page text,
                                registration_domain text,
                                website_url text,
                                abbreviation text NOT NULL,
                                notification_email text,
                                default_partner_ref boolean DEFAULT false,
                                sflink text,
                                auto_assignable boolean DEFAULT false NOT NULL,
                                default_contact_id bigint,
                                default_job_creator boolean DEFAULT false NOT NULL,
                                source_partner boolean DEFAULT false NOT NULL,
                                job_creator boolean DEFAULT false NOT NULL,
                                employer_id bigint,
                                redirect_partner_id bigint,
                                public_api_key_hash character varying(255),
                                public_api_authorities text,
                                public_id character varying(22),
                                accepted_data_processing_agreement_id character varying(255),
                                accepted_data_processing_agreement_date timestamp with time zone,
                                first_dpa_seen_date timestamp with time zone
);


--
-- Name: partner_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.partner_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: partner_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.partner_id_seq OWNED BY public.partner.id;


--
-- Name: partner_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner_job (
                                    partner_id bigint NOT NULL,
                                    tc_job_id bigint NOT NULL,
                                    contact_id bigint
);


--
-- Name: partner_source_country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.partner_source_country (
                                               partner_id bigint NOT NULL,
                                               country_id bigint NOT NULL
);


--
-- Name: reaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reaction (
                                 id bigint NOT NULL,
                                 chat_post_id bigint,
                                 emoji text
);


--
-- Name: reaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.reaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: reaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.reaction_id_seq OWNED BY public.reaction.id;


--
-- Name: reaction_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reaction_user (
                                      reaction_id bigint NOT NULL,
                                      user_id bigint NOT NULL
);


--
-- Name: root_request; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.root_request (
                                     id bigint NOT NULL,
                                     ip_address text,
                                     partner_abbreviation text,
                                     query_string text,
                                     request_url text,
                                     "timestamp" timestamp without time zone,
                                     utm_campaign text,
                                     utm_content text,
                                     utm_medium text,
                                     utm_source text,
                                     utm_term text,
                                     referrer_param text
);


--
-- Name: root_request_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.root_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: root_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.root_request_id_seq OWNED BY public.root_request.id;


--
-- Name: salesforce_job_opp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salesforce_job_opp (
                                           sf_id text,
                                           closed boolean DEFAULT false NOT NULL,
                                           employer text,
                                           name text,
                                           stage text,
                                           stage_order integer,
                                           account_id text,
                                           owner_id text,
                                           id bigint NOT NULL,
                                           submission_due_date timestamp with time zone,
                                           contact_user_id bigint,
                                           job_summary text,
                                           recruiter_partner_id bigint,
                                           suggested_list_id bigint,
                                           submission_list_id bigint,
                                           created_by bigint,
                                           created_date timestamp with time zone,
                                           published_by bigint,
                                           published_date timestamp with time zone,
                                           updated_by bigint,
                                           updated_date timestamp with time zone,
                                           exclusion_list_id bigint,
                                           description text,
                                           salary_range text,
                                           location text,
                                           location_details text,
                                           benefits text,
                                           language_requirements text,
                                           employment_experience text,
                                           education_requirements text,
                                           skill_requirements text,
                                           occupation_code text,
                                           country_object_id bigint,
                                           job_opp_intake_id bigint,
                                           hiring_commitment text,
                                           employer_website text,
                                           employer_hired_internationally text,
                                           opportunity_score text,
                                           won boolean DEFAULT false NOT NULL,
                                           employer_description text,
                                           closing_comments text,
                                           next_step text,
                                           next_step_due_date date,
                                           employer_id bigint,
                                           evergreen boolean DEFAULT false NOT NULL,
                                           evergreen_child_id bigint,
                                           skip_candidate_search boolean DEFAULT false NOT NULL,
                                           jd_file_text text
);


--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.salesforce_job_opp_tc_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: salesforce_job_opp_tc_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.salesforce_job_opp_tc_job_id_seq OWNED BY public.salesforce_job_opp.id;


--
-- Name: saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.saved_list (
                                   id bigint NOT NULL,
                                   status text NOT NULL,
                                   name text NOT NULL,
                                   fixed boolean DEFAULT false NOT NULL,
                                   watcher_ids text,
                                   created_by bigint NOT NULL,
                                   created_date timestamp with time zone NOT NULL,
                                   updated_by bigint,
                                   updated_date timestamp with time zone,
                                   saved_search_id bigint,
                                   global boolean DEFAULT false NOT NULL,
                                   saved_search_source_id bigint,
                                   displayed_fields_long text,
                                   displayed_fields_short text,
                                   folderlink text,
                                   foldercvlink text,
                                   folderjdlink text,
                                   registered_job boolean DEFAULT false NOT NULL,
                                   description text,
                                   published_doc_link text,
                                   tc_short_name text,
                                   sf_opp_is_closed boolean DEFAULT false NOT NULL,
                                   job_id bigint,
                                   file_jd_name text,
                                   file_jd_link text,
                                   file_joi_name text,
                                   file_joi_link text,
                                   file_interview_guidance_name text,
                                   file_interview_guidance_link text,
                                   file_mou_name text,
                                   file_mou_link text,
                                   public_id character varying(22)
);


--
-- Name: saved_list_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.saved_list_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: saved_list_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.saved_list_id_seq OWNED BY public.saved_list.id;


--
-- Name: saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.saved_search (
                                     id bigint NOT NULL,
                                     status text NOT NULL,
                                     name text NOT NULL,
                                     keyword text,
                                     statuses text,
                                     gender text,
                                     occupation_ids text,
                                     or_profile_keyword text,
                                     verified_occupation_ids text,
                                     verified_occupation_search_type text,
                                     nationality_ids text,
                                     nationality_search_type text,
                                     country_ids text,
                                     other_language_id bigint,
                                     un_registered boolean,
                                     last_modified_from date,
                                     last_modified_to date,
                                     created_from date,
                                     created_to date,
                                     min_age integer,
                                     max_age integer,
                                     education_major_ids text,
                                     created_by bigint NOT NULL,
                                     created_date timestamp with time zone NOT NULL,
                                     updated_by bigint,
                                     updated_date timestamp with time zone,
                                     min_education_level integer,
                                     english_min_spoken_level integer,
                                     english_min_written_level integer,
                                     other_min_spoken_level integer,
                                     other_min_written_level integer,
                                     type text,
                                     fixed boolean DEFAULT false NOT NULL,
                                     reviewable boolean DEFAULT false NOT NULL,
                                     watcher_ids text,
                                     global boolean DEFAULT false NOT NULL,
                                     default_search boolean DEFAULT false NOT NULL,
                                     simple_query_string text,
                                     min_yrs integer,
                                     max_yrs integer,
                                     displayed_fields_long text,
                                     displayed_fields_short text,
                                     default_save_selection_list_id bigint,
                                     description text,
                                     survey_type_ids text,
                                     exclusion_list_id bigint,
                                     partner_ids text,
                                     job_id bigint,
                                     country_search_type text,
                                     rego_referrer_param text,
                                     any_opps boolean,
                                     closed_opps boolean,
                                     relocated_opps boolean,
                                     mini_intake_completed boolean,
                                     full_intake_completed boolean,
                                     unhcr_statuses text,
                                     list_all_ids text,
                                     list_all_search_type text,
                                     list_any_ids text,
                                     list_any_search_type text,
                                     potential_duplicate boolean,
                                     public_id character varying(22),
                                     include_pending_terms_candidates boolean,
                                     candidate_numbers text
);


--
-- Name: saved_search_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.saved_search_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: saved_search_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.saved_search_id_seq OWNED BY public.saved_search.id;


--
-- Name: search_join; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.search_join (
                                    id bigint NOT NULL,
                                    search_id bigint NOT NULL,
                                    child_search_id bigint NOT NULL,
                                    search_type text NOT NULL
);


--
-- Name: search_join_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.search_join_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: search_join_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.search_join_id_seq OWNED BY public.search_join.id;


--
-- Name: service_assignment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_assignment (
                                           id bigint NOT NULL,
                                           provider character varying(255) NOT NULL,
                                           service_code character varying(255) NOT NULL,
                                           resource_id bigint,
                                           candidate_id bigint NOT NULL,
                                           actor_id bigint,
                                           status character varying(255) NOT NULL,
                                           assigned_at timestamp with time zone NOT NULL,
                                           created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: service_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_assignment_id_seq OWNED BY public.service_assignment.id;


--
-- Name: service_resource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_resource (
                                         id bigint NOT NULL,
                                         provider character varying(255) NOT NULL,
                                         service_code character varying(255) NOT NULL,
                                         resource_code character varying(255),
                                         status character varying(255) NOT NULL,
                                         expires_at timestamp with time zone,
                                         sent_at timestamp with time zone,
                                         created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: service_resource_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_resource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: service_resource_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.service_resource_id_seq OWNED BY public.service_resource.id;


--
-- Name: shedlock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.shedlock (
                                 name character varying(64) NOT NULL,
                                 lock_until timestamp(3) without time zone,
                                 locked_at timestamp(3) without time zone,
                                 locked_by character varying(255)
);


--
-- Name: skills_esco_en; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills_esco_en (
                                       concepttype text,
                                       concepturi text NOT NULL,
                                       skilltype text,
                                       reuselevel text,
                                       preferredlabel text,
                                       altlabels text,
                                       hiddenlabels text,
                                       status text,
                                       modifieddate text,
                                       scopenote text,
                                       definition text,
                                       inscheme text,
                                       description text
);


--
-- Name: skills_tech_onet_en; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills_tech_onet_en (
                                            id bigint NOT NULL,
                                            "O*NET-SOC CODE" text,
                                            example text,
                                            "Commodity Code" text,
                                            "Commodity Title" text,
                                            "Hot Technology" text,
                                            "In Demand" text
);


--
-- Name: skills_tech_onet_en_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skills_tech_onet_en_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: skills_tech_onet_en_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skills_tech_onet_en_id_seq OWNED BY public.skills_tech_onet_en.id;


--
-- Name: survey_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.survey_type (
                                    id bigint NOT NULL,
                                    name text NOT NULL,
                                    status text DEFAULT 'active'::text NOT NULL
);


--
-- Name: survey_type_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.survey_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: survey_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.survey_type_id_seq OWNED BY public.survey_type.id;


--
-- Name: system_language; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_language (
                                        id bigint NOT NULL,
                                        language text NOT NULL,
                                        label text NOT NULL,
                                        status text DEFAULT 'active'::text NOT NULL,
                                        created_by bigint,
                                        created_date timestamp with time zone,
                                        updated_by bigint,
                                        updated_date timestamp with time zone
);


--
-- Name: system_language_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.system_language_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: system_language_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.system_language_id_seq OWNED BY public.system_language.id;


--
-- Name: task; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task (
                             id bigint NOT NULL,
                             admin boolean DEFAULT false NOT NULL,
                             created_by bigint NOT NULL,
                             created_date timestamp with time zone NOT NULL,
                             days_to_complete integer,
                             description text,
                             doc_link text,
                             name text NOT NULL,
                             optional boolean DEFAULT false NOT NULL,
                             task_type text,
                             updated_by bigint,
                             updated_date timestamp with time zone,
                             upload_subfolder_name text,
                             upload_type text,
                             uploadable_file_types text,
                             candidate_answer_field text,
                             display_name text,
                             explicit_allowed_answers text,
                             required_metadata jsonb,
                             candidate_form_id bigint
);


--
-- Name: task_assignment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_assignment (
                                        id bigint NOT NULL,
                                        abandoned_date timestamp with time zone,
                                        activated_by bigint NOT NULL,
                                        activated_date timestamp with time zone NOT NULL,
                                        candidate_id bigint NOT NULL,
                                        candidate_notes text,
                                        completed_date timestamp with time zone,
                                        deactivated_by bigint,
                                        deactivated_date timestamp with time zone,
                                        due_date timestamp with time zone,
                                        related_list_id bigint,
                                        status text,
                                        task_id bigint NOT NULL,
                                        task_type text
);


--
-- Name: task_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_assignment_id_seq OWNED BY public.task_assignment.id;


--
-- Name: task_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.task_id_seq OWNED BY public.task.id;


--
-- Name: task_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.task_saved_list (
                                        task_id bigint NOT NULL,
                                        saved_list_id bigint NOT NULL
);


--
-- Name: translation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.translation (
                                    id bigint NOT NULL,
                                    object_id bigint NOT NULL,
                                    object_type text NOT NULL,
                                    language text NOT NULL,
                                    value text NOT NULL,
                                    created_by bigint,
                                    created_date timestamp with time zone,
                                    updated_by bigint,
                                    updated_date timestamp with time zone
);


--
-- Name: translation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: translation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.translation_id_seq OWNED BY public.translation.id;


--
-- Name: user_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_job (
                                 user_id bigint NOT NULL,
                                 tc_job_id bigint NOT NULL
);


--
-- Name: user_saved_list; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_list (
                                        user_id bigint NOT NULL,
                                        saved_list_id bigint NOT NULL
);


--
-- Name: user_saved_search; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_saved_search (
                                          user_id bigint NOT NULL,
                                          saved_search_id bigint NOT NULL
);


--
-- Name: user_source_country; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_source_country (
                                            user_id bigint NOT NULL,
                                            country_id bigint NOT NULL
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
                              id bigint NOT NULL,
                              username text,
                              first_name text,
                              last_name text,
                              email text,
                              role text NOT NULL,
                              status text NOT NULL,
                              password_enc text,
                              last_login timestamp with time zone,
                              created_by bigint,
                              created_date timestamp with time zone,
                              updated_by bigint,
                              updated_date timestamp with time zone,
                              reset_token character varying(100),
                              reset_token_issued_date timestamp without time zone,
                              password_updated_date timestamp without time zone,
                              read_only boolean DEFAULT false NOT NULL,
                              using_mfa boolean DEFAULT false NOT NULL,
                              mfa_secret text,
                              host_domain text,
                              partner_id bigint NOT NULL,
                              approver_id bigint,
                              purpose text,
                              job_creator boolean DEFAULT false NOT NULL,
                              email_verified boolean DEFAULT false,
                              email_verification_token character varying(255),
                              email_verification_token_issued_time timestamp without time zone
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- Name: candidate id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate ALTER COLUMN id SET DEFAULT nextval('public.candidate_id_seq'::regclass);


--
-- Name: candidate_attachment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment ALTER COLUMN id SET DEFAULT nextval('public.candidate_attachment_id_seq'::regclass);


--
-- Name: candidate_certification id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification ALTER COLUMN id SET DEFAULT nextval('public.candidate_certification_id_seq'::regclass);


--
-- Name: candidate_citizenship id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship ALTER COLUMN id SET DEFAULT nextval('public.candidate_citizenship_id_seq'::regclass);


--
-- Name: candidate_coupon_code id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code ALTER COLUMN id SET DEFAULT nextval('public.candidate_coupon_code_id_seq'::regclass);


--
-- Name: candidate_dependant id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant ALTER COLUMN id SET DEFAULT nextval('public.candidate_dependant_id_seq'::regclass);


--
-- Name: candidate_destination id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination ALTER COLUMN id SET DEFAULT nextval('public.candidate_destination_id_seq'::regclass);


--
-- Name: candidate_education id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education ALTER COLUMN id SET DEFAULT nextval('public.candidate_education_id_seq'::regclass);


--
-- Name: candidate_exam id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam ALTER COLUMN id SET DEFAULT nextval('public.candidate_exam_id_seq'::regclass);


--
-- Name: candidate_form id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form ALTER COLUMN id SET DEFAULT nextval('public.candidate_form_id_seq'::regclass);


--
-- Name: candidate_job_experience id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience ALTER COLUMN id SET DEFAULT nextval('public.candidate_job_experience_id_seq'::regclass);


--
-- Name: candidate_language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language ALTER COLUMN id SET DEFAULT nextval('public.candidate_language_id_seq'::regclass);


--
-- Name: candidate_note id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note ALTER COLUMN id SET DEFAULT nextval('public.candidate_note_id_seq'::regclass);


--
-- Name: candidate_occupation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation ALTER COLUMN id SET DEFAULT nextval('public.candidate_occupation_id_seq'::regclass);


--
-- Name: candidate_opportunity id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity ALTER COLUMN id SET DEFAULT nextval('public.candidate_opportunity_id_seq'::regclass);


--
-- Name: candidate_property_definition id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition ALTER COLUMN id SET DEFAULT nextval('public.candidate_property_definition_id_seq'::regclass);


--
-- Name: candidate_review_item id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item ALTER COLUMN id SET DEFAULT nextval('public.candidate_review_item_id_seq'::regclass);


--
-- Name: candidate_skill id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill ALTER COLUMN id SET DEFAULT nextval('public.candidate_skill_id_seq'::regclass);


--
-- Name: candidate_visa_check id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_check_id_seq'::regclass);


--
-- Name: candidate_visa_job_check id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check ALTER COLUMN id SET DEFAULT nextval('public.candidate_visa_job_check_id_seq'::regclass);


--
-- Name: chat_post id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post ALTER COLUMN id SET DEFAULT nextval('public.chat_post_id_seq'::regclass);


--
-- Name: country id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country ALTER COLUMN id SET DEFAULT nextval('public.country_id_seq'::regclass);


--
-- Name: duolingo_coupon id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon ALTER COLUMN id SET DEFAULT nextval('public.duolingo_coupon_id_seq'::regclass);


--
-- Name: duolingo_extra_fields id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields ALTER COLUMN id SET DEFAULT nextval('public.duolingo_extra_fields_id_seq'::regclass);


--
-- Name: education_level id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_level ALTER COLUMN id SET DEFAULT nextval('public.education_level_id_seq'::regclass);


--
-- Name: education_major id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_major ALTER COLUMN id SET DEFAULT nextval('public.education_major_id_seq'::regclass);


--
-- Name: employer id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer ALTER COLUMN id SET DEFAULT nextval('public.employer_id_seq'::regclass);


--
-- Name: export_column id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column ALTER COLUMN id SET DEFAULT nextval('public.export_column_id_seq'::regclass);


--
-- Name: help_link id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link ALTER COLUMN id SET DEFAULT nextval('public.help_link_id_seq'::regclass);


--
-- Name: industry id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry ALTER COLUMN id SET DEFAULT nextval('public.industry_id_seq'::regclass);


--
-- Name: job_chat id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat ALTER COLUMN id SET DEFAULT nextval('public.job_chat_id_seq'::regclass);


--
-- Name: job_opp_intake id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_opp_intake ALTER COLUMN id SET DEFAULT nextval('public.job_opp_intake_id_seq'::regclass);


--
-- Name: language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language ALTER COLUMN id SET DEFAULT nextval('public.language_id_seq'::regclass);


--
-- Name: language_level id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language_level ALTER COLUMN id SET DEFAULT nextval('public.language_level_id_seq'::regclass);


--
-- Name: link_preview id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview ALTER COLUMN id SET DEFAULT nextval('public.link_preview_id_seq'::regclass);


--
-- Name: nationality id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nationality ALTER COLUMN id SET DEFAULT nextval('public.nationality_id_seq'::regclass);


--
-- Name: occupation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occupation ALTER COLUMN id SET DEFAULT nextval('public.occupation_id_seq'::regclass);


--
-- Name: offer_to_assist id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist ALTER COLUMN id SET DEFAULT nextval('public.offer_to_assist_id_seq'::regclass);


--
-- Name: partner id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner ALTER COLUMN id SET DEFAULT nextval('public.partner_id_seq'::regclass);


--
-- Name: reaction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction ALTER COLUMN id SET DEFAULT nextval('public.reaction_id_seq'::regclass);


--
-- Name: root_request id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.root_request ALTER COLUMN id SET DEFAULT nextval('public.root_request_id_seq'::regclass);


--
-- Name: salesforce_job_opp id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp ALTER COLUMN id SET DEFAULT nextval('public.salesforce_job_opp_tc_job_id_seq'::regclass);


--
-- Name: saved_list id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list ALTER COLUMN id SET DEFAULT nextval('public.saved_list_id_seq'::regclass);


--
-- Name: saved_search id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search ALTER COLUMN id SET DEFAULT nextval('public.saved_search_id_seq'::regclass);


--
-- Name: search_join id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join ALTER COLUMN id SET DEFAULT nextval('public.search_join_id_seq'::regclass);


--
-- Name: service_assignment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment ALTER COLUMN id SET DEFAULT nextval('public.service_assignment_id_seq'::regclass);


--
-- Name: service_resource id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_resource ALTER COLUMN id SET DEFAULT nextval('public.service_resource_id_seq'::regclass);


--
-- Name: skills_tech_onet_en id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_tech_onet_en ALTER COLUMN id SET DEFAULT nextval('public.skills_tech_onet_en_id_seq'::regclass);


--
-- Name: survey_type id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.survey_type ALTER COLUMN id SET DEFAULT nextval('public.survey_type_id_seq'::regclass);


--
-- Name: system_language id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language ALTER COLUMN id SET DEFAULT nextval('public.system_language_id_seq'::regclass);


--
-- Name: task id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task ALTER COLUMN id SET DEFAULT nextval('public.task_id_seq'::regclass);


--
-- Name: task_assignment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment ALTER COLUMN id SET DEFAULT nextval('public.task_assignment_id_seq'::regclass);


--
-- Name: translation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation ALTER COLUMN id SET DEFAULT nextval('public.translation_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: batch_job_execution_context batch_job_execution_context_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_context
    ADD CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_execution batch_job_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution
    ADD CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id);


--
-- Name: batch_job_instance batch_job_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_instance
    ADD CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id);


--
-- Name: batch_step_execution_context batch_step_execution_context_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution_context
    ADD CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id);


--
-- Name: batch_step_execution batch_step_execution_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution
    ADD CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id);


--
-- Name: candidate_certification candidate_certification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_pkey PRIMARY KEY (id);


--
-- Name: candidate_citizenship candidate_citizenship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_pkey PRIMARY KEY (id);


--
-- Name: candidate_coupon_code candidate_coupon_code_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_pkey PRIMARY KEY (id);


--
-- Name: candidate_dependant candidate_dependant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_pkey PRIMARY KEY (id);


--
-- Name: candidate_destination candidate_destination_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_pkey PRIMARY KEY (id);


--
-- Name: candidate_education candidate_education_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_pkey PRIMARY KEY (id);


--
-- Name: candidate_exam candidate_exam_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_pkey PRIMARY KEY (id);


--
-- Name: candidate_attachment candidate_file_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_pkey PRIMARY KEY (id);


--
-- Name: candidate_form_instance candidate_form_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_pkey PRIMARY KEY (candidate_id, form_id);


--
-- Name: candidate_form candidate_form_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form
    ADD CONSTRAINT candidate_form_pkey PRIMARY KEY (id);


--
-- Name: candidate_job_experience candidate_job_experience_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_pkey PRIMARY KEY (id);


--
-- Name: candidate_json_cache candidate_json_cache_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_json_cache
    ADD CONSTRAINT candidate_json_cache_pkey PRIMARY KEY (candidate_id);


--
-- Name: candidate_language candidate_language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_pkey PRIMARY KEY (id);


--
-- Name: candidate_note candidate_note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_pkey PRIMARY KEY (id);


--
-- Name: candidate_occupation candidate_occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_pkey PRIMARY KEY (id);


--
-- Name: candidate_opportunity candidate_opportunity_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_pkey PRIMARY KEY (id);


--
-- Name: candidate candidate_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_pkey PRIMARY KEY (id);


--
-- Name: candidate_property_definition candidate_property_definition_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition
    ADD CONSTRAINT candidate_property_definition_name_key UNIQUE (name);


--
-- Name: candidate_property_definition candidate_property_definition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property_definition
    ADD CONSTRAINT candidate_property_definition_pkey PRIMARY KEY (id);


--
-- Name: candidate_property candidate_property_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_pkey PRIMARY KEY (candidate_id, name);


--
-- Name: candidate_review_item candidate_review_item_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_job_check candidate_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_pkey PRIMARY KEY (id);


--
-- Name: candidate_saved_list candidate_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_pkey PRIMARY KEY (candidate_id, saved_list_id);


--
-- Name: candidate_skill candidate_skill_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_pkey PRIMARY KEY (id);


--
-- Name: candidate_visa_check candidate_visa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_pkey PRIMARY KEY (id);


--
-- Name: chat_post chat_post_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_pkey PRIMARY KEY (id);


--
-- Name: chatbot_message chatbot_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chatbot_message
    ADD CONSTRAINT chatbot_message_pkey PRIMARY KEY (id);


--
-- Name: country_nationality_join country_nationality_join_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_pkey PRIMARY KEY (country_id, nationality_id);


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: duolingo_coupon duolingo_coupon_coupon_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_coupon_code_key UNIQUE (coupon_code);


--
-- Name: duolingo_coupon duolingo_coupon_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT duolingo_coupon_pkey PRIMARY KEY (id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_pkey PRIMARY KEY (id);


--
-- Name: education_level education_level_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_level
    ADD CONSTRAINT education_level_pkey PRIMARY KEY (id);


--
-- Name: education_major education_major_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.education_major
    ADD CONSTRAINT education_major_pkey PRIMARY KEY (id);


--
-- Name: employer employer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_pkey PRIMARY KEY (id);


--
-- Name: export_column export_column_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_pkey PRIMARY KEY (id);



--
-- Name: help_link help_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_pkey PRIMARY KEY (id);


--
-- Name: industry industry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry
    ADD CONSTRAINT industry_pkey PRIMARY KEY (id);


--
-- Name: job_chat job_chat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_pkey PRIMARY KEY (id);


--
-- Name: job_chat_user job_chat_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_pkey PRIMARY KEY (job_chat_id, user_id);


--
-- Name: batch_job_instance job_inst_un; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_instance
    ADD CONSTRAINT job_inst_un UNIQUE (job_name, job_key);


--
-- Name: job_opp_intake job_opp_intake_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_opp_intake
    ADD CONSTRAINT job_opp_intake_pkey PRIMARY KEY (id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_pkey PRIMARY KEY (tc_job_id, saved_search_id);


--
-- Name: language_level language_level_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language_level
    ADD CONSTRAINT language_level_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: link_preview link_preview_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_pkey PRIMARY KEY (id);


--
-- Name: nationality nationality_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nationality
    ADD CONSTRAINT nationality_pkey PRIMARY KEY (id);


--
-- Name: occupation occupation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occupation
    ADD CONSTRAINT occupation_pkey PRIMARY KEY (id);


--
-- Name: offer_to_assist offer_to_assist_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_pkey PRIMARY KEY (id);


--
-- Name: partner_job partner_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_pkey PRIMARY KEY (partner_id, tc_job_id);


--
-- Name: partner partner_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_pkey PRIMARY KEY (id);


--
-- Name: partner_source_country partner_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_pkey PRIMARY KEY (partner_id, country_id);


--
-- Name: reaction reaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_pkey PRIMARY KEY (id);


--
-- Name: reaction_user reaction_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_pkey PRIMARY KEY (reaction_id, user_id);


--
-- Name: root_request root_request_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.root_request
    ADD CONSTRAINT root_request_pkey PRIMARY KEY (id);


--
-- Name: salesforce_job_opp salesforce_job_opp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_pkey PRIMARY KEY (id);


--
-- Name: saved_list saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_pkey PRIMARY KEY (id);


--
-- Name: saved_search saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_pkey PRIMARY KEY (id);


--
-- Name: search_join search_join_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_pkey PRIMARY KEY (id);


--
-- Name: service_assignment service_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_pkey PRIMARY KEY (id);


--
-- Name: service_resource service_resource_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_resource
    ADD CONSTRAINT service_resource_pkey PRIMARY KEY (id);


--
-- Name: shedlock shedlock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.shedlock
    ADD CONSTRAINT shedlock_pkey PRIMARY KEY (name);


--
-- Name: skills_esco_en skills_esco_en_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_esco_en
    ADD CONSTRAINT skills_esco_en_pk PRIMARY KEY (concepturi);


--
-- Name: skills_tech_onet_en skills_tech_onet_en_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills_tech_onet_en
    ADD CONSTRAINT skills_tech_onet_en_pkey PRIMARY KEY (id);


--
-- Name: survey_type survey_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.survey_type
    ADD CONSTRAINT survey_type_pkey PRIMARY KEY (id);


--
-- Name: system_language system_language_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_pkey PRIMARY KEY (id);


--
-- Name: task_assignment task_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_pkey PRIMARY KEY (id);


--
-- Name: task task_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_pkey PRIMARY KEY (id);


--
-- Name: task_saved_list task_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_pkey PRIMARY KEY (saved_list_id, task_id);


--
-- Name: translation translation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_pkey PRIMARY KEY (id);


--
-- Name: candidate_form unique_name; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form
    ADD CONSTRAINT unique_name UNIQUE (name);


--
-- Name: candidate_occupation uq_candidate_occupation; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT uq_candidate_occupation UNIQUE (candidate_id, occupation_id);


--
-- Name: salesforce_job_opp uq_tc_job_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT uq_tc_job_id UNIQUE (id);


--
-- Name: candidate uq_user_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT uq_user_id UNIQUE (user_id);


--
-- Name: user_job user_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_pkey PRIMARY KEY (user_id, tc_job_id);


--
-- Name: user_saved_list user_saved_list_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_pkey PRIMARY KEY (user_id, saved_list_id);


--
-- Name: user_saved_search user_saved_search_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_pkey PRIMARY KEY (user_id, saved_search_id);


--
-- Name: user_source_country user_source_country_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_pkey PRIMARY KEY (user_id, country_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: candidate_candidate_number_uindex; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX candidate_candidate_number_uindex ON public.candidate USING btree (candidate_number);


--
-- Name: candidate_dependant_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_dependant_candidate_id_idx ON public.candidate_dependant USING btree (candidate_id);


--
-- Name: candidate_form_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_form_name_idx ON public.candidate_form USING btree (name);


--
-- Name: candidate_number_status_country_active_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_number_status_country_active_idx ON public.candidate USING btree (candidate_number, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_public_id_idx ON public.candidate USING btree (public_id);


--
-- Name: candidate_registered_by_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_registered_by_idx ON public.candidate USING btree (registered_by);


--
-- Name: candidate_saved_list_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_saved_list_id_idx ON public.candidate_saved_list USING btree (saved_list_id);


--
-- Name: candidate_sflink_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_sflink_index ON public.candidate USING btree (sflink);


--
-- Name: candidate_status_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_status_index ON public.candidate USING btree (status);


--
-- Name: candidate_user_status_country_active_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_user_status_country_active_idx ON public.candidate USING btree (user_id, status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_visa_job_check_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX candidate_visa_job_check_candidate_id_idx ON public.candidate_visa_job_check USING btree (candidate_id);


--
-- Name: chatbot_message_question_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_question_id_idx ON public.chatbot_message USING btree (question_id);


--
-- Name: chatbot_message_referenced_faq_ids_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_referenced_faq_ids_idx ON public.chatbot_message USING gin (referenced_faq_ids);


--
-- Name: chatbot_message_session_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX chatbot_message_session_id_idx ON public.chatbot_message USING btree (session_id);


--
-- Name: country_iso_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX country_iso_code_idx ON public.country USING btree (iso_code);


--
-- Name: idx_candidate_external_id_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_external_id_country ON public.candidate USING btree (lower(external_id), country_id);


--
-- Name: idx_candidate_json_cache_version; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_json_cache_version ON public.candidate_json_cache USING btree (data_version);


--
-- Name: idx_candidate_phone_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_phone_lower ON public.candidate USING btree (lower(phone)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_status_country; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_status_country ON public.candidate USING btree (status, country_id) WHERE (status <> 'deleted'::text);


--
-- Name: idx_candidate_whatsapp_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_candidate_whatsapp_lower ON public.candidate USING btree (lower(whatsapp)) WHERE (status <> 'deleted'::text);


--
-- Name: idx_users_email_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email_lower ON public.users USING btree (lower(email));


--
-- Name: ip_address_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ip_address_idx ON public.root_request USING btree (ip_address);


--
-- Name: job_chat_candidate_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_candidate_id_idx ON public.job_chat USING btree (candidate_id);


--
-- Name: job_chat_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_id_idx ON public.chat_post USING btree (job_chat_id);


--
-- Name: job_chat_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_job_id_idx ON public.job_chat USING btree (job_id);


--
-- Name: job_chat_source_partner_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_source_partner_id_idx ON public.job_chat USING btree (source_partner_id);


--
-- Name: job_chat_type_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX job_chat_type_idx ON public.job_chat USING btree (type);


--
-- Name: language_iso_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX language_iso_code_idx ON public.language USING btree (iso_code);


--
-- Name: occupation_isco08_code_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX occupation_isco08_code_idx ON public.occupation USING btree (isco08_code);


--
-- Name: offer_to_assist_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX offer_to_assist_public_id_idx ON public.offer_to_assist USING btree (public_id);


--
-- Name: partner_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX partner_public_id_idx ON public.partner USING btree (public_id);


--
-- Name: sa_assigned_per_resource_uq_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX sa_assigned_per_resource_uq_idx ON public.service_assignment USING btree (resource_id) WHERE (((status)::text = 'ASSIGNED'::text) AND (resource_id IS NOT NULL));


--
-- Name: sa_candidate_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_candidate_idx ON public.service_assignment USING btree (candidate_id);


--
-- Name: sa_provider_service_candidate_status_time_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_provider_service_candidate_status_time_idx ON public.service_assignment USING btree (provider, service_code, candidate_id, status, assigned_at DESC);


--
-- Name: sa_resource_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sa_resource_idx ON public.service_assignment USING btree (resource_id);


--
-- Name: salesforce_job_opp_tc_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX salesforce_job_opp_tc_job_id_idx ON public.salesforce_job_opp USING btree (id);


--
-- Name: saved_list_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_list_public_id_idx ON public.saved_list USING btree (public_id);


--
-- Name: saved_search_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_search_id_idx ON public.user_saved_search USING btree (saved_search_id);


--
-- Name: saved_search_public_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX saved_search_public_id_idx ON public.saved_search USING btree (public_id);


--
-- Name: sr_provider_resource_uq_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX sr_provider_resource_uq_idx ON public.service_resource USING btree (provider, resource_code) WHERE (resource_code IS NOT NULL);


--
-- Name: sr_provider_sc_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sr_provider_sc_status_idx ON public.service_resource USING btree (provider, service_code, status);


--
-- Name: sr_provider_service_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX sr_provider_service_idx ON public.service_resource USING btree (provider, service_code, id);


--
-- Name: task_name_uindex; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX task_name_uindex ON public.task USING btree (name);


--
-- Name: ts_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ts_idx ON public.candidate_job_experience USING gin (ts);


--
-- Name: ts_text_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX ts_text_idx ON public.candidate USING gin (ts_text);


--
-- Name: user_job_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_job_id_idx ON public.user_job USING btree (tc_job_id);


--
-- Name: user_lower_username_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_lower_username_idx ON public.users USING btree (lower(username));


--
-- Name: user_saved_list_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX user_saved_list_id_idx ON public.user_saved_list USING btree (saved_list_id);


--
-- Name: users_full_name_rev_trgm_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_full_name_rev_trgm_idx ON public.users USING gin (lower(((TRIM(BOTH FROM last_name) || ' '::text) || TRIM(BOTH FROM first_name))) public.gin_trgm_ops);


--
-- Name: users_full_name_trgm_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_full_name_trgm_idx ON public.users USING gin (lower(((TRIM(BOTH FROM first_name) || ' '::text) || TRIM(BOTH FROM last_name))) public.gin_trgm_ops);


--
-- Name: users_lower_first_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_lower_first_name_idx ON public.users USING btree (lower(first_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_lower_last_name_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_lower_last_name_idx ON public.users USING btree (lower(last_name)) WHERE (status <> 'deleted'::text);


--
-- Name: users_partner_id_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_partner_id_index ON public.users USING btree (partner_id);


--
-- Name: users_username_active_status_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX users_username_active_status_idx ON public.users USING btree (lower(username)) WHERE (status <> 'deleted'::text);


--
-- Name: candidate_attachment candidate_attachment_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_attachment_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_attachment FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate candidate_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_bump_version BEFORE UPDATE ON public.candidate FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_self_version();


--
-- Name: candidate_certification candidate_certification_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_certification_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_certification FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_citizenship candidate_citizenship_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_citizenship_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_citizenship FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_coupon_code candidate_coupon_code_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_coupon_code_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_coupon_code FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_dependant candidate_dependant_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_dependant_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_dependant FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_destination candidate_destination_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_destination_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_destination FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_education candidate_education_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_education_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_education FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_exam candidate_exam_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_exam_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_exam FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_job_experience candidate_job_experience_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_job_experience_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_job_experience FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_language candidate_language_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_language_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_language FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_note candidate_note_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_note_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_note FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_occupation candidate_occupation_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_occupation_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_occupation FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_opportunity candidate_opportunity_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_opportunity_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_opportunity FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_property candidate_property_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_property_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_property FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_review_item candidate_review_item_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_review_item_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_review_item FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_skill candidate_skill_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_skill_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_skill FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_visa_check candidate_visa_check_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_visa_check_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_visa_check FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: candidate_visa_job_check candidate_visa_job_check_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER candidate_visa_job_check_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.candidate_visa_job_check FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: task_assignment task_assignment_bump_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER task_assignment_bump_version AFTER INSERT OR DELETE OR UPDATE ON public.task_assignment FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_ref_version();


--
-- Name: users user_bump_candidate_version; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER user_bump_candidate_version AFTER INSERT OR DELETE OR UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION public.bump_candidate_on_user_change();


--
-- Name: candidate_attachment candidate_attachment_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_attachment_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_birth_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_birth_country_id_fkey FOREIGN KEY (birth_country_id) REFERENCES public.country(id);


--
-- Name: candidate_certification candidate_certification_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_certification
    ADD CONSTRAINT candidate_certification_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_citizenship candidate_citizenship_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_citizenship
    ADD CONSTRAINT candidate_citizenship_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_coupon_code candidate_coupon_code_offer_to_assist_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_coupon_code
    ADD CONSTRAINT candidate_coupon_code_offer_to_assist_id_fkey FOREIGN KEY (offer_to_assist_id) REFERENCES public.offer_to_assist(id);


--
-- Name: candidate candidate_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_dependant candidate_dependant_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_dependant
    ADD CONSTRAINT candidate_dependant_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_destination candidate_destination_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_destination
    ADD CONSTRAINT candidate_destination_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_driving_license_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_driving_license_country_id_fkey FOREIGN KEY (driving_license_country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_education candidate_education_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_education candidate_education_major_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_education
    ADD CONSTRAINT candidate_education_major_id_fkey FOREIGN KEY (major_id) REFERENCES public.education_major(id);


--
-- Name: candidate_exam candidate_exam_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_exam
    ADD CONSTRAINT candidate_exam_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_attachment candidate_file_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_attachment
    ADD CONSTRAINT candidate_file_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_form_instance candidate_form_instance_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_form_instance candidate_form_instance_form_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_form_instance
    ADD CONSTRAINT candidate_form_instance_form_id_fkey FOREIGN KEY (form_id) REFERENCES public.candidate_form(id);


--
-- Name: candidate candidate_full_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_full_intake_completed_by_fkey FOREIGN KEY (full_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_job_experience candidate_job_experience_candidate_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_candidate_occupation_id_fkey FOREIGN KEY (candidate_occupation_id) REFERENCES public.candidate_occupation(id);


--
-- Name: candidate_job_experience candidate_job_experience_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_job_experience
    ADD CONSTRAINT candidate_job_experience_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_language candidate_language_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_language candidate_language_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_language_id_fkey FOREIGN KEY (language_id) REFERENCES public.language(id);


--
-- Name: candidate_language candidate_language_spoken_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_spoken_level_id_fkey FOREIGN KEY (spoken_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate_language candidate_language_written_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_language
    ADD CONSTRAINT candidate_language_written_level_id_fkey FOREIGN KEY (written_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_max_education_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_max_education_level_id_fkey FOREIGN KEY (max_education_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_mini_intake_completed_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_mini_intake_completed_by_fkey FOREIGN KEY (mini_intake_completed_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.country(id);


--
-- Name: candidate candidate_nationalityold_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_nationalityold_id_fkey FOREIGN KEY (nationalityold_id) REFERENCES public.nationality(id);


--
-- Name: candidate_note candidate_note_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_note candidate_note_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_note candidate_note_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_note
    ADD CONSTRAINT candidate_note_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_occupation candidate_occupation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_occupation candidate_occupation_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_occupation candidate_occupation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_occupation
    ADD CONSTRAINT candidate_occupation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_opportunity candidate_opportunity_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_opportunity candidate_opportunity_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_opportunity candidate_opportunity_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_opportunity
    ADD CONSTRAINT candidate_opportunity_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_partner_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_candidate_id_fkey FOREIGN KEY (partner_candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_partner_edu_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_edu_level_id_fkey FOREIGN KEY (partner_edu_level_id) REFERENCES public.education_level(id);


--
-- Name: candidate candidate_partner_english_level_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_english_level_id_fkey FOREIGN KEY (partner_english_level_id) REFERENCES public.language_level(id);


--
-- Name: candidate candidate_partner_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_partner_occupation_id_fkey FOREIGN KEY (partner_occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_property candidate_property_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_property candidate_property_related_task_assignment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_property
    ADD CONSTRAINT candidate_property_related_task_assignment_id_fkey FOREIGN KEY (related_task_assignment_id) REFERENCES public.task_assignment(id);


--
-- Name: candidate candidate_registered_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_registered_by_fkey FOREIGN KEY (registered_by) REFERENCES public.partner(id);


--
-- Name: candidate candidate_relocated_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_relocated_country_id_fkey FOREIGN KEY (relocated_country_id) REFERENCES public.country(id);


--
-- Name: candidate_review_item candidate_review_item_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_review_item candidate_review_item_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_review_item candidate_review_item_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: candidate_review_item candidate_review_item_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_review_item
    ADD CONSTRAINT candidate_review_item_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_role_occupation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_role_occupation_id_fkey FOREIGN KEY (occupation_id) REFERENCES public.occupation(id);


--
-- Name: candidate_saved_list candidate_saved_list_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_saved_list candidate_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_saved_list candidate_saved_list_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_saved_list
    ADD CONSTRAINT candidate_saved_list_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_cv_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_cv_attachment_id_fkey FOREIGN KEY (shareable_cv_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate candidate_shareable_doc_attachment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_shareable_doc_attachment_id_fkey FOREIGN KEY (shareable_doc_attachment_id) REFERENCES public.candidate_attachment(id);


--
-- Name: candidate_skill candidate_skill_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_skill
    ADD CONSTRAINT candidate_skill_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate candidate_survey_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_survey_type_id_fkey FOREIGN KEY (survey_type_id) REFERENCES public.survey_type(id);


--
-- Name: candidate candidate_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: candidate candidate_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT candidate_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: candidate_visa_check candidate_visa_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_visa_check candidate_visa_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: candidate_visa_check candidate_visa_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_candidate_visa_check_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_candidate_visa_check_id_fkey FOREIGN KEY (candidate_visa_check_id) REFERENCES public.candidate_visa_check(id);


--
-- Name: candidate_visa_job_check candidate_visa_job_check_job_opp_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_job_check
    ADD CONSTRAINT candidate_visa_job_check_job_opp_id_fkey FOREIGN KEY (job_opp_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: candidate_visa_check candidate_visa_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate_visa_check
    ADD CONSTRAINT candidate_visa_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: chat_post chat_post_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: chat_post chat_post_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_post
    ADD CONSTRAINT chat_post_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: country_nationality_join country_nationality_join_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: country_nationality_join country_nationality_join_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.country_nationality_join
    ADD CONSTRAINT country_nationality_join_nationality_id_fkey FOREIGN KEY (nationality_id) REFERENCES public.nationality(id);


--
-- Name: duolingo_extra_fields duolingo_extra_fields_candidate_exam_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_extra_fields
    ADD CONSTRAINT duolingo_extra_fields_candidate_exam_id_fkey FOREIGN KEY (candidate_exam_id) REFERENCES public.candidate_exam(id);


--
-- Name: employer employer_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: employer employer_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: employer employer_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.employer
    ADD CONSTRAINT employer_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: export_column export_column_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: export_column export_column_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.export_column
    ADD CONSTRAINT export_column_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: duolingo_coupon fk_candidate; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.duolingo_coupon
    ADD CONSTRAINT fk_candidate FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: candidate fk_candidate_privacy_policy_partner; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidate
    ADD CONSTRAINT fk_candidate_privacy_policy_partner FOREIGN KEY (accepted_privacy_policy_partner_id) REFERENCES public.partner(id) ON DELETE SET NULL;


--
-- Name: help_link help_link_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: help_link help_link_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: help_link help_link_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.help_link
    ADD CONSTRAINT help_link_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: job_chat job_chat_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: job_chat job_chat_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: job_chat job_chat_source_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_source_partner_id_fkey FOREIGN KEY (source_partner_id) REFERENCES public.partner(id);


--
-- Name: job_chat job_chat_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat
    ADD CONSTRAINT job_chat_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: job_chat_user job_chat_user_job_chat_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_job_chat_id_fkey FOREIGN KEY (job_chat_id) REFERENCES public.job_chat(id);


--
-- Name: job_chat_user job_chat_user_last_read_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_last_read_post_id_fkey FOREIGN KEY (last_read_post_id) REFERENCES public.chat_post(id);


--
-- Name: job_chat_user job_chat_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_chat_user
    ADD CONSTRAINT job_chat_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: batch_job_execution_context job_exec_ctx_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_context
    ADD CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution_params job_exec_params_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution_params
    ADD CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_step_execution job_exec_step_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution
    ADD CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id);


--
-- Name: batch_job_execution job_inst_exec_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_job_execution
    ADD CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES public.batch_job_instance(job_instance_id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: job_suggested_saved_search job_suggested_saved_search_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.job_suggested_saved_search
    ADD CONSTRAINT job_suggested_saved_search_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: link_preview link_preview_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.link_preview
    ADD CONSTRAINT link_preview_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: offer_to_assist offer_to_assist_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: offer_to_assist offer_to_assist_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: offer_to_assist offer_to_assist_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.offer_to_assist
    ADD CONSTRAINT offer_to_assist_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: partner partner_default_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_default_contact_id_fkey FOREIGN KEY (default_contact_id) REFERENCES public.users(id);


--
-- Name: partner partner_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: partner_job partner_job_contact_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES public.users(id);


--
-- Name: partner_job partner_job_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: partner_job partner_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_job
    ADD CONSTRAINT partner_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: partner partner_redirect_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner
    ADD CONSTRAINT partner_redirect_partner_id_fkey FOREIGN KEY (redirect_partner_id) REFERENCES public.partner(id);


--
-- Name: partner_source_country partner_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: partner_source_country partner_source_country_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.partner_source_country
    ADD CONSTRAINT partner_source_country_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: reaction reaction_chat_post_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction
    ADD CONSTRAINT reaction_chat_post_id_fkey FOREIGN KEY (chat_post_id) REFERENCES public.chat_post(id);


--
-- Name: reaction_user reaction_user_reaction_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_reaction_id_fkey FOREIGN KEY (reaction_id) REFERENCES public.reaction(id);


--
-- Name: reaction_user reaction_user_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reaction_user
    ADD CONSTRAINT reaction_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_contact_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_contact_user_id_fkey FOREIGN KEY (contact_user_id) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_country_object_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_country_object_id_fkey FOREIGN KEY (country_object_id) REFERENCES public.country(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_employer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_employer_id_fkey FOREIGN KEY (employer_id) REFERENCES public.employer(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_evergreen_child_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_evergreen_child_id_fkey FOREIGN KEY (evergreen_child_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_job_opp_intake_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_job_opp_intake_id_fkey FOREIGN KEY (job_opp_intake_id) REFERENCES public.job_opp_intake(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_published_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_published_by_fkey FOREIGN KEY (published_by) REFERENCES public.users(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_recruiter_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_recruiter_partner_id_fkey FOREIGN KEY (recruiter_partner_id) REFERENCES public.partner(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_submission_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_submission_list_id_fkey FOREIGN KEY (submission_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_suggested_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_suggested_list_id_fkey FOREIGN KEY (suggested_list_id) REFERENCES public.saved_list(id);


--
-- Name: salesforce_job_opp salesforce_job_opp_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salesforce_job_opp
    ADD CONSTRAINT salesforce_job_opp_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_list saved_list_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_list saved_list_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_saved_search_source_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_saved_search_source_id_fkey FOREIGN KEY (saved_search_source_id) REFERENCES public.saved_search(id);


--
-- Name: saved_list saved_list_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_list
    ADD CONSTRAINT saved_list_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: saved_search saved_search_default_save_selection_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_default_save_selection_list_id_fkey FOREIGN KEY (default_save_selection_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_exclusion_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_exclusion_list_id_fkey FOREIGN KEY (exclusion_list_id) REFERENCES public.saved_list(id);


--
-- Name: saved_search saved_search_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_job_id_fkey FOREIGN KEY (job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: saved_search saved_search_other_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_other_language_id_fkey FOREIGN KEY (other_language_id) REFERENCES public.language(id);


--
-- Name: saved_search saved_search_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.saved_search
    ADD CONSTRAINT saved_search_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: search_join search_join_child_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_child_search_id_fkey FOREIGN KEY (child_search_id) REFERENCES public.saved_search(id);


--
-- Name: search_join search_join_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.search_join
    ADD CONSTRAINT search_join_search_id_fkey FOREIGN KEY (search_id) REFERENCES public.saved_search(id);


--
-- Name: service_assignment service_assignment_actor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES public.users(id);


--
-- Name: service_assignment service_assignment_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: service_assignment service_assignment_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_assignment
    ADD CONSTRAINT service_assignment_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.service_resource(id) ON DELETE SET NULL;


--
-- Name: batch_step_execution_context step_exec_ctx_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.batch_step_execution_context
    ADD CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES public.batch_step_execution(step_execution_id);


--
-- Name: system_language system_language_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: system_language system_language_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_language
    ADD CONSTRAINT system_language_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_activated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_activated_by_fkey FOREIGN KEY (activated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_candidate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_candidate_id_fkey FOREIGN KEY (candidate_id) REFERENCES public.candidate(id);


--
-- Name: task_assignment task_assignment_deactivated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_deactivated_by_fkey FOREIGN KEY (deactivated_by) REFERENCES public.users(id);


--
-- Name: task_assignment task_assignment_related_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_related_list_id_fkey FOREIGN KEY (related_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_assignment task_assignment_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_assignment
    ADD CONSTRAINT task_assignment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_candidate_form_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_candidate_form_id_fkey FOREIGN KEY (candidate_form_id) REFERENCES public.candidate_form(id);


--
-- Name: task task_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: task_saved_list task_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: task_saved_list task_saved_list_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task_saved_list
    ADD CONSTRAINT task_saved_list_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.task(id);


--
-- Name: task task_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.task
    ADD CONSTRAINT task_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: translation translation_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: translation translation_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.translation
    ADD CONSTRAINT translation_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Name: user_job user_job_tc_job_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_tc_job_id_fkey FOREIGN KEY (tc_job_id) REFERENCES public.salesforce_job_opp(id);


--
-- Name: user_job user_job_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_job
    ADD CONSTRAINT user_job_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_list user_saved_list_saved_list_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_saved_list_id_fkey FOREIGN KEY (saved_list_id) REFERENCES public.saved_list(id);


--
-- Name: user_saved_list user_saved_list_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_list
    ADD CONSTRAINT user_saved_list_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_saved_search user_saved_search_saved_search_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_saved_search_id_fkey FOREIGN KEY (saved_search_id) REFERENCES public.saved_search(id);


--
-- Name: user_saved_search user_saved_search_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_saved_search
    ADD CONSTRAINT user_saved_search_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_source_country user_source_country_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_country_id_fkey FOREIGN KEY (country_id) REFERENCES public.country(id);


--
-- Name: user_source_country user_source_country_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_source_country
    ADD CONSTRAINT user_source_country_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: users users_created_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_created_by_fkey FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: users users_partner_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_partner_id_fkey FOREIGN KEY (partner_id) REFERENCES public.partner(id);


--
-- Name: users users_updated_by_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_updated_by_fkey FOREIGN KEY (updated_by) REFERENCES public.users(id);


--
-- Country static data
--

insert into public.country (id, name, status, iso_code)
values  (0, 'Unknown', 'inactive', null),
        (6180, 'Afghanistan', 'active', 'AF'),
        (6181, 'Albania', 'active', 'AL'),
        (6182, 'Algeria', 'active', 'DZ'),
        (6183, 'American Samoa', 'active', 'AS'),
        (6184, 'Andorra', 'active', 'AD'),
        (6185, 'Angola', 'active', 'AO'),
        (6186, 'Anguilla', 'active', 'AI'),
        (6188, 'Argentina', 'active', 'AR'),
        (6189, 'Armenia', 'active', 'AM'),
        (6190, 'Aruba', 'active', 'AW'),
        (6191, 'Australia', 'active', 'AU'),
        (6192, 'Austria', 'active', 'AT'),
        (6193, 'Azerbaijan', 'active', 'AZ'),
        (6194, 'Bahamas', 'active', 'BS'),
        (6195, 'Bahrain', 'active', 'BH'),
        (6196, 'Bangladesh', 'active', 'BD'),
        (6197, 'Barbados', 'active', 'BB'),
        (6198, 'Belarus', 'active', 'BY'),
        (6199, 'Belgium', 'active', 'BE'),
        (6200, 'Belize', 'active', 'BZ'),
        (6201, 'Benin', 'active', 'BJ'),
        (6202, 'Bermuda', 'active', 'BM'),
        (6203, 'Bhutan', 'active', 'BT'),
        (6204, 'Bolivia', 'active', 'BO'),
        (6206, 'Botswana', 'active', 'BW'),
        (6207, 'Brazil', 'active', 'BR'),
        (6210, 'Brunei', 'active', 'BN'),
        (6211, 'Bulgaria', 'active', 'BG'),
        (6212, 'Burkina Faso', 'active', 'BF'),
        (6214, 'Cambodia', 'active', 'KH'),
        (6215, 'Cameroon', 'active', 'CM'),
        (6216, 'Canada', 'active', 'CA'),
        (6217, 'Cape Verde', 'active', 'CV'),
        (6220, 'Central African Republic', 'active', 'CF'),
        (6221, 'Chad', 'active', 'TD'),
        (6222, 'Chile', 'active', 'CL'),
        (6223, 'China', 'active', 'CN'),
        (6226, 'Colombia', 'active', 'CO'),
        (6230, 'Cook Islands', 'active', 'CK'),
        (6231, 'Costa Rica', 'active', 'CR'),
        (6233, 'Croatia', 'active', 'HR'),
        (6234, 'Cuba', 'active', 'CU'),
        (6236, 'Cyprus', 'active', 'CY'),
        (6237, 'Czechia', 'active', 'CZ'),
        (6239, 'Djibouti', 'active', 'DJ'),
        (6240, 'Dominica', 'active', 'DM'),
        (6241, 'Dominican Republic', 'active', 'DO'),
        (6242, 'Ecuador', 'active', 'EC'),
        (6243, 'Egypt', 'active', 'EG'),
        (6245, 'Equatorial Guinea', 'active', 'GQ'),
        (6246, 'Eritrea', 'active', 'ER'),
        (6247, 'Estonia', 'active', 'EE'),
        (6248, 'Ethiopia', 'active', 'ET'),
        (6249, 'Falkland Islands', 'active', 'FK'),
        (6250, 'Faroe Islands', 'active', 'FO'),
        (6251, 'Fiji', 'active', 'FJ'),
        (6253, 'France', 'active', 'FR'),
        (6254, 'French Guiana', 'active', 'GF'),
        (6255, 'French Polynesia', 'active', 'PF'),
        (6256, 'Gabon', 'active', 'GA'),
        (6257, 'Gambia', 'active', 'GM'),
        (6259, 'Germany', 'active', 'DE'),
        (6260, 'Ghana', 'active', 'GH'),
        (6262, 'Greece', 'active', 'GR'),
        (6263, 'Greenland', 'active', 'GL'),
        (6264, 'Grenada', 'active', 'GD'),
        (6265, 'Guadeloupe', 'active', 'GP'),
        (6266, 'Guam', 'active', 'GU'),
        (6269, 'Guinea', 'active', 'GN'),
        (6270, 'Guinea-Bissau', 'active', 'GW'),
        (6271, 'Guyana', 'active', 'GY'),
        (6272, 'Haiti', 'active', 'HT'),
        (6273, 'Honduras', 'active', 'HN'),
        (6275, 'Hungary', 'active', 'HU'),
        (6277, 'India', 'active', 'IN'),
        (6278, 'Indonesia', 'active', 'ID'),
        (6279, 'Iran', 'active', 'IR'),
        (6280, 'Iraq', 'active', 'IQ'),
        (6281, 'Ireland', 'active', 'IE'),
        (6284, 'Italy', 'active', 'IT'),
        (6285, 'Jamaica', 'active', 'JM'),
        (6286, 'Japan', 'active', 'JP'),
        (6289, 'Kazakhstan', 'active', 'KZ'),
        (6290, 'Kenya', 'active', 'KE'),
        (6291, 'Kiribati', 'active', 'KI'),
        (6292, 'Kuwait', 'active', 'KW'),
        (6293, 'Kyrgyzstan', 'active', 'KG'),
        (6295, 'Latvia', 'active', 'LV'),
        (6297, 'Lesotho', 'active', 'LS'),
        (6298, 'Liberia', 'active', 'LR'),
        (6299, 'Libya', 'active', 'LY'),
        (6300, 'Liechtenstein', 'active', 'LI'),
        (6301, 'Lithuania', 'active', 'LT'),
        (6302, 'Luxembourg', 'active', 'LU'),
        (6305, 'Madagascar', 'active', 'MG'),
        (6307, 'Malaysia', 'active', 'MY'),
        (6308, 'Maldives', 'active', 'MV'),
        (6309, 'Mali', 'active', 'ML'),
        (6310, 'Malta', 'active', 'MT'),
        (6311, 'Marshall Islands', 'active', 'MH'),
        (6312, 'Martinique', 'active', 'MQ'),
        (6314, 'Mauritius', 'active', 'MU'),
        (6316, 'Mexico', 'active', 'MX'),
        (6319, 'Monaco', 'active', 'MC'),
        (6320, 'Mongolia', 'active', 'MN'),
        (6321, 'Montenegro', 'active', 'ME'),
        (6322, 'Montserrat', 'active', 'MS'),
        (6323, 'Morocco', 'active', 'MA'),
        (6326, 'Namibia', 'active', 'NA'),
        (6327, 'Nauru', 'active', 'NR'),
        (6328, 'Nepal', 'active', 'NP'),
        (6329, 'Netherlands', 'active', 'NL'),
        (6330, 'New Caledonia', 'active', 'NC'),
        (6331, 'New Zealand', 'active', 'NZ'),
        (6332, 'Nicaragua', 'active', 'NI'),
        (6333, 'Niger', 'active', 'NE'),
        (6334, 'Nigeria', 'active', 'NG'),
        (6335, 'Niue', 'active', 'NU'),
        (6337, 'North Korea', 'active', 'KP'),
        (6339, 'Norway', 'active', 'NO'),
        (6340, 'Oman', 'active', 'OM'),
        (6341, 'Pakistan', 'active', 'PK'),
        (6342, 'Palau', 'active', 'PW'),
        (6344, 'Panama', 'active', 'PA'),
        (6345, 'Papua New Guinea', 'active', 'PG'),
        (6179, 'United Kingdom', 'active', 'GB'),
        (9430, 'Urdu', 'active', null),
        (6370, 'Seychelles', 'active', 'SC'),
        (6371, 'Sierra Leone', 'active', 'SL'),
        (1000, 'Stateless', 'active', null),
        (6372, 'Singapore', 'active', 'SG'),
        (6375, 'Slovenia', 'active', 'SI'),
        (6376, 'Solomon Islands', 'active', 'SB'),
        (6377, 'Somalia', 'active', 'SO'),
        (6378, 'South Africa', 'active', 'ZA'),
        (6410, 'Uruguay', 'active', 'UY'),
        (6411, 'Uzbekistan', 'active', 'UZ'),
        (6412, 'Vanuatu', 'active', 'VU'),
        (6379, 'South Korea', 'active', 'KR'),
        (6413, 'Vatican City', 'active', 'VA'),
        (6187, 'Antigua & Barbuda', 'active', 'AG'),
        (6205, 'Bosnia & Herzegovina', 'active', 'BA'),
        (6213, 'Burundi', 'active', 'BI'),
        (6227, 'Comoros', 'active', 'KM'),
        (1001, 'Côte d’Ivoire', 'active', 'CI'),
        (6238, 'Denmark', 'active', 'DK'),
        (6244, 'El Salvador', 'active', 'SV'),
        (6252, 'Finland', 'active', 'FI'),
        (6258, 'Georgia', 'active', 'GE'),
        (6267, 'Guatemala', 'active', 'GT'),
        (6274, 'Hong Kong SAR China', 'active', 'HK'),
        (6276, 'Iceland', 'active', 'IS'),
        (6283, 'Israel', 'active', 'IL'),
        (6288, 'Jordan', 'active', 'JO'),
        (6296, 'Lebanon', 'active', 'LB'),
        (6306, 'Malawi', 'active', 'MW'),
        (6313, 'Mauritania', 'active', 'MR'),
        (6324, 'Mozambique', 'active', 'MZ'),
        (9565, 'Myanmar (Burma)', 'active', 'MM'),
        (6338, 'Northern Mariana Islands', 'active', 'MP'),
        (6343, 'Palestinian Territories', 'active', 'PS'),
        (6346, 'Paraguay', 'active', 'PY'),
        (6347, 'Peru', 'active', 'PE'),
        (6348, 'Philippines', 'active', 'PH'),
        (6349, 'Poland', 'active', 'PL'),
        (6350, 'Portugal', 'active', 'PT'),
        (6351, 'Puerto Rico', 'active', 'PR'),
        (6352, 'Qatar', 'active', 'QA'),
        (6354, 'Romania', 'active', 'RO'),
        (6355, 'Russia', 'active', 'RU'),
        (6356, 'Rwanda', 'active', 'RW'),
        (6364, 'Samoa', 'active', 'WS'),
        (6365, 'San Marino', 'active', 'SM'),
        (6366, 'São Tomé & Príncipe', 'active', 'ST'),
        (6367, 'Saudi Arabia', 'active', 'SA'),
        (6368, 'Senegal', 'active', 'SN'),
        (6369, 'Serbia', 'active', 'RS'),
        (6380, 'South Sudan', 'active', 'SS'),
        (6381, 'Spain', 'active', 'ES'),
        (6382, 'Sri Lanka', 'active', 'LK'),
        (6359, 'St. Kitts & Nevis', 'active', 'KN'),
        (6360, 'St. Lucia', 'active', 'LC'),
        (6363, 'St. Vincent & Grenadines', 'active', 'VC'),
        (6383, 'Sudan', 'active', 'SD'),
        (6384, 'Suriname', 'active', 'SR'),
        (6386, 'Swaziland', 'active', 'SZ'),
        (6387, 'Sweden', 'active', 'SE'),
        (6388, 'Switzerland', 'active', 'CH'),
        (6389, 'Syria', 'active', 'SY'),
        (6390, 'Taiwan', 'active', 'TW'),
        (6391, 'Tajikistan', 'active', 'TJ'),
        (6392, 'Tanzania', 'active', 'TZ'),
        (6393, 'Thailand', 'active', 'TH'),
        (6395, 'Togo', 'active', 'TG'),
        (6397, 'Tonga', 'active', 'TO'),
        (6398, 'Trinidad & Tobago', 'active', 'TT'),
        (6399, 'Tunisia', 'active', 'TN'),
        (6401, 'Turkmenistan', 'active', 'TM'),
        (6403, 'Tuvalu', 'active', 'TV'),
        (6405, 'Uganda', 'active', 'UG'),
        (6406, 'Ukraine', 'active', 'UA'),
        (6407, 'United Arab Emirates', 'active', 'AE'),
        (6178, 'United States', 'active', 'US'),
        (6414, 'Venezuela', 'active', 'VE'),
        (6415, 'Vietnam', 'active', 'VN'),
        (6417, 'Western Sahara', 'active', 'EH'),
        (6418, 'Yemen', 'active', 'YE'),
        (6419, 'Zambia', 'active', 'ZM'),
        (6420, 'Zimbabwe', 'active', 'ZW'),
        (10000, 'Côte d’Ivoire', 'inactive', 'CI'),
        (6400, 'Türkiye', 'active', 'TR'),
        (10002, 'Laos', 'active', 'LA'),
        (10003, 'Cayman Islands', 'active', 'KY'),
        (10001, 'Congo (the Democratic Republic of the)', 'active', 'CD'),
        (1002, 'Congo (the)', 'active', 'CG'),
        (10004, 'Slovakia', 'active', 'SK'),
        (10005, 'Turks and Caicos Islands', 'active', 'TC');


--
-- Language static data
--
insert into public.language (id, name, status, iso_code)
values  (0, 'Unknown', 'inactive', null),
        (10000, 'Acholi', 'active', null),
        (10005, 'Ashante', 'active', null),
        (10006, 'Asl', 'active', null),
        (10007, 'Assyrian', 'active', null),
        (10009, 'Azeri', 'active', null),
        (10010, 'Bajuni', 'active', null),
        (10012, 'Behdini', 'active', null),
        (10013, 'Belorussian', 'active', null),
        (10014, 'Bengali', 'active', null),
        (10015, 'Berber', 'active', null),
        (10017, 'Bravanese', 'active', null),
        (10020, 'Cakchiquel', 'active', null),
        (10021, 'Cambodian', 'active', null),
        (10022, 'Cantonese', 'active', null),
        (10024, 'Chaldean', 'active', null),
        (10026, 'Chao-chow', 'active', null),
        (10027, 'Chavacano', 'active', null),
        (10028, 'Chin', 'active', null),
        (10029, 'Chuukese', 'active', null),
        (10033, 'Dakota', 'active', null),
        (10036, 'Dinka', 'active', null),
        (10037, 'Diula', 'active', null),
        (10039, 'Edo', 'active', null),
        (10042, 'Fante', 'active', null),
        (10043, 'Fijian Hindi', 'active', null),
        (10045, 'Flemish', 'active', null),
        (10046, 'French Canadian', 'active', null),
        (10047, 'Fukienese', 'active', null),
        (10048, 'Fula', 'active', null),
        (10049, 'Fulani', 'active', null),
        (10050, 'Fuzhou', 'active', null),
        (10051, 'Ga', 'active', null),
        (10052, 'Gaddang', 'active', null),
        (10053, 'Gaelic', 'active', null),
        (10054, 'Gaelic-irish', 'active', null),
        (10055, 'Gaelic-scottish', 'active', null),
        (10057, 'Gorani', 'active', null),
        (10060, 'Hakka', 'active', null),
        (10061, 'Hakka-chinese', 'active', null),
        (10065, 'Hmong', 'active', null),
        (10067, 'Ibanag', 'active', null),
        (10068, 'Ibo', 'active', null),
        (10071, 'Ilocano', 'active', null),
        (10074, 'Jakartanese', 'active', null),
        (10077, 'Kanjobal', 'active', null),
        (10078, 'Karen', 'active', null),
        (10079, 'Karenni', 'active', null),
        (10084, 'Kirundi', 'active', null),
        (10086, 'Kosovan', 'active', null),
        (10087, 'Kotokoli', 'active', null),
        (10088, 'Krio', 'active', null),
        (10090, 'Kurmanji', 'active', null),
        (10092, 'Lakota', 'active', null),
        (10093, 'Laotian', 'active', null),
        (10097, 'Luganda', 'active', null),
        (10098, 'Luo', 'active', null),
        (10099, 'Maay', 'active', null),
        (10104, 'Mandarin', 'active', null),
        (10105, 'Mandingo', 'active', null),
        (10106, 'Mandinka', 'active', null),
        (10109, 'Mien', 'active', null),
        (10110, 'Mina', 'active', null),
        (10111, 'Mirpuri', 'active', null),
        (10112, 'Mixteco', 'active', null),
        (10113, 'Moldavan', 'active', null),
        (10115, 'Montenegrin', 'active', null),
        (10117, 'Neapolitan', 'active', null),
        (10119, 'Nigerian Pidgin', 'active', null),
        (10122, 'Pahari', 'active', null),
        (10123, 'Papago', 'active', null),
        (10124, 'Papiamento', 'active', null),
        (10125, 'Patois', 'active', null),
        (10126, 'Pidgin English', 'active', null),
        (10128, 'Portug.creole', 'active', null),
        (10129, 'Pothwari', 'active', null),
        (10130, 'Pulaar', 'active', null),
        (10132, 'Putian', 'active', null),
        (10133, 'Quichua', 'active', null),
        (10137, 'Shanghainese', 'active', null),
        (10139, 'Sichuan', 'active', null),
        (10140, 'Sicilian', 'active', null),
        (10141, 'Sinhalese', 'active', null),
        (10143, 'Sorani', 'active', null),
        (10144, 'Sudanese Arabic', 'active', null),
        (10145, 'Susu', 'active', null),
        (10148, 'Sylhetti', 'active', null),
        (10002, 'Akan', 'active', 'ak'),
        (10003, 'Albanian', 'active', 'sq'),
        (10004, 'Amharic', 'active', 'am'),
        (10008, 'Azerbaijani', 'active', 'az'),
        (10011, 'Basque', 'active', 'eu'),
        (10016, 'Bosnian', 'active', 'bs'),
        (10019, 'Burmese', 'active', 'my'),
        (10023, 'Catalan', 'active', 'ca'),
        (10025, 'Chamorro', 'active', 'ch'),
        (10030, 'Cree', 'active', 'cr'),
        (10031, 'Croatian', 'active', 'hr'),
        (10032, 'Czech', 'active', 'cs'),
        (10038, 'Dutch', 'active', 'nl'),
        (10040, 'Estonian', 'active', 'et'),
        (10041, 'Ewe', 'active', 'ee'),
        (10044, 'Finnish', 'active', 'fi'),
        (10056, 'Georgian', 'active', 'ka'),
        (10059, 'Haitian Creole', 'active', 'ht'),
        (10062, 'Hausa', 'active', 'ha'),
        (10063, 'Hebrew', 'active', 'he'),
        (10064, 'Hindi', 'active', 'hi'),
        (10066, 'Hungarian', 'active', 'hu'),
        (10069, 'Icelandic', 'active', 'is'),
        (10070, 'Igbo', 'active', 'ig'),
        (10076, 'Javanese', 'active', 'jv'),
        (10080, 'Kashmiri', 'active', 'ks'),
        (10094, 'Latvian', 'active', 'lv'),
        (10095, 'Lingala', 'active', 'ln'),
        (10096, 'Lithuanian', 'active', 'lt'),
        (10100, 'Macedonian', 'active', 'mk'),
        (10101, 'Malay', 'active', 'ms'),
        (10102, 'Malayalam', 'active', 'ml'),
        (10103, 'Maltese', 'active', 'mt'),
        (10107, 'Marathi', 'active', 'mr'),
        (10108, 'Marshallese', 'active', 'mh'),
        (10114, 'Mongolian', 'active', 'mn'),
        (10116, 'Navajo', 'active', 'nv'),
        (10118, 'Nepali', 'active', 'ne'),
        (10120, 'Norwegian', 'active', 'no'),
        (10121, 'Oromo', 'active', 'om'),
        (10127, 'Polish', 'active', 'pl'),
        (10131, 'Punjabi', 'active', 'pa'),
        (10134, 'Romanian', 'active', 'ro'),
        (10135, 'Samoan', 'active', 'sm'),
        (10136, 'Serbian', 'active', 'sr'),
        (10138, 'Shona', 'active', 'sn'),
        (10142, 'Slovak', 'active', 'sk'),
        (10146, 'Swahili', 'active', 'sw'),
        (10147, 'Swedish', 'active', 'sv'),
        (10149, 'Tagalog', 'active', 'tl'),
        (10150, 'Taiwanese', 'active', null),
        (10156, 'Tigre', 'active', null),
        (10158, 'Toishanese', 'active', null),
        (10160, 'Toucouleur', 'active', null),
        (10161, 'Trique', 'active', null),
        (10162, 'Tshiluba', 'active', null),
        (10169, 'Visayan', 'active', null),
        (10174, 'Yupik', 'active', null),
        (8790, 'Sudanese', 'active', null),
        (9429, 'Farsi', 'active', null),
        (9433, 'Kurdish-Kurmanji', 'active', null),
        (9434, 'Kurdish-Badini', 'active', null),
        (9435, 'Kurdish-Sorani', 'active', null),
        (9431, 'Dari', 'active', 'fa'),
        (10001, 'Afrikaans', 'active', 'af'),
        (343, 'Arabic', 'active', 'ar'),
        (347, 'Armenian', 'active', 'hy'),
        (10018, 'Bulgarian', 'active', 'bg'),
        (10034, 'Danish', 'active', 'da'),
        (342, 'English', 'active', 'en'),
        (344, 'French', 'active', 'fr'),
        (7343, 'German', 'active', 'de'),
        (9419, 'Greek', 'active', 'el'),
        (10058, 'Gujarati', 'active', 'gu'),
        (10072, 'Indonesian', 'active', 'in'),
        (10073, 'Inuktitut', 'active', 'iu'),
        (8787, 'Italian', 'active', 'it'),
        (10075, 'Japanese', 'active', 'ja'),
        (10081, 'Kazakh', 'active', 'kk'),
        (10082, 'Kikuyu', 'active', 'ki'),
        (10083, 'Kinyarwanda', 'active', 'rw'),
        (10085, 'Korean', 'active', 'ko'),
        (10089, 'Kurdish', 'active', 'ku'),
        (10091, 'Kyrgyz', 'active', 'ky'),
        (9432, 'Pashto', 'active', 'ps'),
        (8788, 'Portuguese ', 'active', 'pt'),
        (345, 'Russian ', 'active', 'ru'),
        (8789, 'Somali', 'active', 'so'),
        (346, 'Spanish', 'active', 'es'),
        (10151, 'Tajik', 'active', 'tg'),
        (10152, 'Tamil', 'active', 'ta'),
        (10153, 'Telugu', 'active', 'te'),
        (10154, 'Thai', 'active', 'th'),
        (10155, 'Tibetan', 'active', 'bo'),
        (10157, 'Tigrinya', 'active', 'ti'),
        (10159, 'Tongan', 'active', 'to'),
        (8791, 'Turkish', 'active', 'tr'),
        (10163, 'Twi', 'active', 'tw'),
        (10164, 'Ukrainian', 'active', 'uk'),
        (10165, 'Urdu', 'active', 'ur'),
        (10166, 'Uyghur', 'active', 'ug'),
        (10167, 'Uzbek', 'active', 'uz'),
        (10168, 'Vietnamese', 'active', 'vi'),
        (10170, 'Welsh', 'active', 'cy'),
        (10171, 'Wolof', 'active', 'wo'),
        (10172, 'Yiddish', 'active', 'ji'),
        (10173, 'Yoruba', 'active', 'yo'),
        (10175, 'Balochi (Southern Balochi)', 'active', 'bcc'),
        (10176, 'Balochi (Western Balochi)', 'active', 'bgn'),
        (10177, 'Balochi (Eastern Balochi)', 'active', 'bgp'),
        (10178, 'brahui', 'active', 'brh'),
        (10179, 'circassian (adyghe)', 'active', 'ady'),
        (10180, 'circassian (kabardian)', 'active', 'kbd'),
        (10181, 'fur', 'active', 'fvr'),
        (10182, 'latin', 'active', 'la'),
        (10183, 'nauruan', 'active', 'nau'),
        (10184, 'rohingya', 'active', 'rhg'),
        (10185, 'sindhi', 'active', 'snd'),
        (10186, 'syriac', 'active', 'syr'),
        (10187, 'zaghawa', 'active', 'zag'),
        (10188, 'International Sign', 'active', 'ils');

insert into public.education_level (id, name, level, status, education_type, isced_code)
values  (0, 'Unknown', 0, 'inactive', null, null),
        (271, 'Primary School', 10, 'active', null, null),
        (273, 'Secondary School Degree or Equivalent', 40, 'active', null, null),
        (8136, 'Some University', 90, 'active', 'Bachelor', null),
        (8137, 'Associate Degree', 70, 'active', 'Associate', null),
        (8138, 'Bachelor''s Degree', 100, 'active', 'Bachelor', null),
        (8139, 'Master''s Degree', 110, 'active', 'Masters', null),
        (8140, 'Doctoral Degree', 120, 'active', 'Doctoral', null),
        (9437, 'Some Secondary School', 20, 'active', null, null),
        (9439, 'Vocational Degree', 60, 'active', 'Vocational', null),
        (9440, 'Some Vocational Training ', 50, 'active', null, null),
        (9563, 'No Formal Education ', 0, 'active', null, null);

insert into public.education_major (id, name, status)
values  (0, 'Unknown', 'inactive'),
        (8713, 'Accounting', 'active'),
        (8714, 'Administrative Development', 'active'),
        (8715, 'Agriculture', 'active'),
        (8716, 'Applied Technology', 'active'),
        (8717, 'Archeology', 'active'),
        (8718, 'Architecture', 'active'),
        (8719, 'Astronomy', 'active'),
        (8720, 'Astrophysics', 'active'),
        (8721, 'Artificial Intelligence', 'active'),
        (8722, 'Biology', 'active'),
        (8723, 'Biochemistry', 'active'),
        (8724, 'Biomaterials', 'active'),
        (8725, 'Biomedical Science', 'active'),
        (8726, 'Business Administration', 'active'),
        (8727, 'Chemistry', 'active'),
        (8728, 'Child Study and Human Development', 'active'),
        (8729, 'Cognitive and Brain Sciences', 'active'),
        (8730, 'Communications and Media', 'active'),
        (8731, 'Computer Science', 'active'),
        (8732, 'Criminology/Criminal Justice', 'active'),
        (8733, 'Dentistry', 'active'),
        (8734, 'Design', 'active'),
        (8735, 'Ecology', 'active'),
        (8736, 'Education', 'active'),
        (8737, 'Engineering', 'active'),
        (8738, 'Environmental Studies', 'active'),
        (8739, 'Environmental Technology', 'active'),
        (8740, 'Epidemiology', 'active'),
        (8741, 'Finance/Banking/Economics', 'active'),
        (8742, 'Fine Arts', 'active'),
        (8743, 'Food Science', 'active'),
        (8744, 'Forest Studies', 'active'),
        (8745, 'Geology', 'active'),
        (8746, 'Global Health/Healthcare', 'active'),
        (8747, 'Hospice/Palliative Care', 'active'),
        (8748, 'Human Resource Management', 'active'),
        (8749, 'Information Technology', 'active'),
        (8750, 'International Relations', 'active'),
        (8751, 'Journalism', 'active'),
        (8752, 'Islamic Law', 'active'),
        (8753, 'Language (ex. French, Spanish, Russian, etc.)', 'active'),
        (8754, 'Laser Research and Applications', 'active'),
        (8755, 'Marketing', 'active'),
        (8756, 'Mathematics', 'active'),
        (8757, 'Medicine', 'active'),
        (8759, 'Nutrition', 'active'),
        (8760, 'Optometry', 'active'),
        (8761, 'Physics', 'active'),
        (8762, 'Pharmacy', 'active'),
        (8763, 'Political Studies and Public Policy', 'active'),
        (8764, 'Psychology', 'active'),
        (8765, 'Social Sciences', 'active'),
        (8766, 'Social Work', 'active'),
        (8767, 'Statistics', 'active'),
        (8768, 'Translation', 'active'),
        (8769, 'Urban Planning and Policy', 'active'),
        (8770, 'Veterinary', 'active'),
        (8778, 'Law', 'active');

insert into public.industry (id, name, status)
values  (0, 'Unknown', 'inactive'),
        (8792, '﻿Accounting/Auditing', 'active'),
        (8793, 'Administration', 'active'),
        (8794, 'Advertising', 'active'),
        (8795, 'Aerospace and Defense', 'active'),
        (8796, 'Agriculture/Forestry/Fishing', 'active'),
        (8797, 'Airlines/Aviation', 'active'),
        (8798, 'Architecture', 'active'),
        (8799, 'Arts/Entertainment/and Media', 'active'),
        (8800, 'Automotive', 'active'),
        (8801, 'Aviation/Marine Refueling', 'active'),
        (8802, 'Banking', 'active'),
        (8803, 'Biotechnology', 'active'),
        (8804, 'Business Support', 'active'),
        (8805, 'Catering/Food Services/Restaurants', 'active'),
        (8806, 'Community/Social Services/and Nonprofit', 'active'),
        (8807, 'Computer/Hardware', 'active'),
        (8808, 'Computer/Software', 'active'),
        (8809, 'Construction', 'active'),
        (8810, 'Construction/Civil Engineering', 'active'),
        (8811, 'Consulting Services', 'active'),
        (8812, 'Contracts/Purchasing', 'active'),
        (8813, 'Customer Service', 'active'),
        (8814, 'Distributions and Logistics', 'active'),
        (8815, 'Education', 'active'),
        (8816, 'Employment Place Agencies/Recruiting ', 'active'),
        (8817, 'Energy ', 'active'),
        (8818, 'Engineering', 'active'),
        (8819, 'Entertainment', 'active'),
        (8820, 'Facilities Management', 'active'),
        (8821, 'Fashion Design ', 'active'),
        (8822, 'Finance/Economics ', 'active'),
        (8823, 'Financial Services ', 'active'),
        (8824, 'Food Production', 'active'),
        (8825, 'Government Sector', 'active'),
        (8826, 'Graphic Design ', 'active'),
        (8827, 'Healthcare', 'active'),
        (8828, 'Hospitality/Tourism/Travel', 'active'),
        (8829, 'Human Resources', 'active'),
        (8830, 'Industrial ', 'active'),
        (8831, 'Information Technology', 'active'),
        (8832, 'Installation', 'active'),
        (8833, 'Insurance', 'active'),
        (8834, 'Interior Design', 'active'),
        (8835, 'Internet/E-commerce', 'active'),
        (8836, 'Islamic Banking', 'active'),
        (8837, 'Journalism ', 'active'),
        (8838, 'Laboratory/QC', 'active'),
        (8839, 'Law Enforcement/Security Services', 'active'),
        (8840, 'Legal', 'active'),
        (8841, 'Management', 'active'),
        (8842, 'Manufacturing and Production', 'active'),
        (8843, 'Marine Services', 'active'),
        (8844, 'Marketing', 'active'),
        (8845, 'Mechanical', 'active'),
        (8846, 'Medical/Hospital', 'active'),
        (8847, 'Merchandising', 'active'),
        (8848, 'Military ', 'active'),
        (8849, 'Mining', 'active'),
        (8850, 'Modeling', 'active'),
        (8851, 'National Gas Distribution', 'active'),
        (8852, 'Nursing', 'active'),
        (8853, 'Oil/Gas', 'active'),
        (8854, 'Personal Care and Service', 'active'),
        (8855, 'Petrochemicals', 'active'),
        (8856, 'Pharmaceutical', 'active'),
        (8857, 'Photography', 'active'),
        (8858, 'Planning', 'active'),
        (8859, 'Public Relations', 'active'),
        (8860, 'Publishing', 'active'),
        (8861, 'Real Estate ', 'active'),
        (8862, 'Retail/Wholesale', 'active'),
        (8863, 'Safety/Environment', 'active'),
        (8864, 'Sales', 'active'),
        (8865, 'Science', 'active'),
        (8866, 'Secretarial', 'active'),
        (8867, 'Shipping', 'active'),
        (8868, 'Sports and Recreation', 'active'),
        (8869, 'Support Services', 'active'),
        (8870, 'Technical/Maintenance', 'active'),
        (8871, 'Telecommunications', 'active'),
        (8872, 'Telemarketing', 'active'),
        (8873, 'Textiles', 'active'),
        (8874, 'Translation', 'active'),
        (8875, 'Transportation', 'active'),
        (8876, 'Utilities ', 'active'),
        (8877, 'Vehicle Inspection ', 'active'),
        (8878, 'Warehousing', 'active');


insert into public.language_level (id, name, status, level, cefr_level)
values  (0, 'Unknown', 'inactive', 0, null),
        (353, 'No Proficiency', 'active', 0, null),
        (349, 'Elementary Proficiency', 'active', 10, 'A2'),
        (350, 'Intermediate Proficiency', 'active', 20, 'B1'),
        (351, 'Full Professional Proficiency', 'active', 30, 'C1'),
        (352, 'Native or Bilingual Proficiency', 'active', 40, 'C2'),
        (500, 'Beginner Proficiency', 'active', 5, 'A1'),
        (501, 'Advanced Proficiency', 'active', 25, 'B2');

insert into public.occupation (id, name, status, isco08_code)
values  (0, 'Unknown', 'active', null),
        (8577, 'Accountant', 'active', '2411'),
        (8484, 'Administrative assistant', 'active', '3343'),
        (8485, 'Agricultural, fishery or related laborer', 'active', '622'),
        (8580, 'Agronomist or related', 'active', '2132'),
        (8581, 'Aircraft pilot or related professional', 'active', '5164'),
        (8582, 'Anthropologist', 'active', '2632'),
        (10000, 'Archaeologist', 'active', '2632'),
        (8584, 'Architect or planner', 'active', '2161'),
        (8585, 'Archivist, curator, or librarian', 'active', '262'),
        (8586, 'Armed forces/military', 'active', '0'),
        (8488, 'Assembler (electrical equipment)', 'active', '8212'),
        (8489, 'Assembler (mechanical-machinery)', 'active', '8211'),
        (8589, 'Assembling labourer', 'active', '9321'),
        (8590, 'Astrologer or related worker', 'active', '5161'),
        (8591, 'Athlete or sportsperson', 'active', '3421'),
        (8492, 'Baker or pastry-cook', 'active', '7512'),
        (8493, 'Blacksmith, tool maker, or forge worker', 'active', '722'),
        (8496, 'Builder (traditional materials)', 'active', '7111'),
        (8498, 'Building construction labourer', 'active', '9313'),
        (8499, 'Business professional not elsewhere classified', 'active', '24'),
        (8502, 'Cartoonist', 'active', '2651'),
        (10001, 'Chef', 'active', '3434'),
        (8504, 'Companion or valet', 'active', '5162'),
        (8505, 'Computer programmer', 'active', '2514'),
        (8507, 'Construction labourer (roads, dams)', 'active', '931'),
        (8511, 'Craftsman (wood, textile, leather)', 'active', '754'),
        (8512, 'Data entry operator', 'active', '4132'),
        (8516, 'Electrician', 'active', '7411'),
        (8517, 'Employment agent or labour contractor', 'active', '3333'),
        (8519, 'Fashion or other model', 'active', '5241'),
        (8521, 'Finance professional', 'active', '1211'),
        (8522, 'Fire-fighter', 'active', '5411'),
        (8524, 'Floor layer or tile setter', 'active', '7122'),
        (8525, 'Food & beverage taster or grader', 'active', '7515'),
        (8527, 'Garbage collector', 'active', '9611'),
        (8529, 'General manager in wholesale or retail', 'active', '1219'),
        (8528, 'General manager (own or small business)', 'active', '1120'),
        (8530, 'Hairdresser, barber, beautician or related', 'active', '514'),
        (8531, 'Healthcare professional (hospice)', 'active', '5322'),
        (8533, 'Housekeeper ', 'active', '5151'),
        (8535, 'Information technology professional', 'active', '351'),
        (8537, 'Livestock Worker', 'active', '6121'),
        (8538, 'Machine Operator (baked-goods, cereal & chocolate-products)', 'active', '816'),
        (8543, 'Mechanic (electrical)', 'active', '7412'),
        (8486, 'Auctioneer', 'active', '3339'),
        (8495, 'Bricklayer', 'active', '711'),
        (8500, 'Butcher', 'active', '7511'),
        (8497, 'Inspector, building', 'active', '3112'),
        (8618, 'Medical and Dental Prosthetic Technicians', 'active', '2261'),
        (8603, 'Surveyor, land', 'active', '2165'),
        (10002, 'Appraiser', 'active', '3315'),
        (10003, 'Dental Assistants and Therapists', 'active', '3251'),
        (10004, 'Stonemason', 'active', '7113'),
        (10005, 'Fishmonger', 'active', '7511'),
        (10006, 'Cartographer', 'active', '2165'),
        (10007, 'Botanist', 'active', '2131'),
        (10008, 'Pathologist', 'active', '2212'),
        (10010, 'Optician, ophthalmic', 'active', '2267'),
        (10011, 'Tuner, musical instrument', 'active', '7312'),
        (10012, 'Geophysicist', 'active', '2114'),
        (10013, 'Engineer, diesel', 'active', '2144'),
        (10014, 'Mechanic, diesel: motor vehicle', 'active', '7231'),
        (10015, 'Mechanic, engine: diesel (except motor vehicle)', 'active', '7233'),
        (10016, 'Scientist, data mining', 'active', '2529'),
        (10017, 'Other Teaching Professionals', 'active', '235'),
        (10018, 'Other Health Associate Professionals', 'active', '325'),
        (10019, 'Other Personal Services Workers', 'active', '516'),
        (8703, 'Student', 'active', null),
        (8595, 'Bookkeeper', 'active', '3313'),
        (8602, 'Carpenter or joiner', 'active', '7115'),
        (8606, 'Chemist', 'active', '2113'),
        (8607, 'Child-care worker', 'active', '5311'),
        (8610, 'Concrete placer, finisher or related', 'active', '7114'),
        (8612, 'Cook', 'active', '5120'),
        (8613, 'Corporate director or chief executive', 'active', '1219'),
        (8614, 'Craft and related trades worker (pottery, hand metal work, glass maker, painter, jewelry etc.)', 'active', '754'),
        (8617, 'Decorator and commercial designer', 'active', '3432'),
        (8619, 'Dentist', 'active', '2261'),
        (8620, 'Dietician & nutritionist', 'active', '2265'),
        (8621, 'Draughtsperson', 'active', '3118'),
        (8622, 'Driver and mobile-plant operator', 'active', '83'),
        (8623, 'Economist', 'active', '2631'),
        (8624, 'Education methods specialist', 'active', '2351'),
        (8632, 'Filmmaker', 'active', '2654'),
        (8593, 'Biologist', 'active', '213'),
        (9426, 'Bachelor''s Degree', 'inactive', null),
        (8678, 'Pharmacologist', 'active', '2131'),
        (8705, 'Teacher', 'active', '23'),
        (8699, 'Sociologist', 'active', '2632'),
        (10020, 'Other Sales Workers', 'active', '524'),
        (10021, 'Other Stationary Plant and Machine Operators', 'active', '818'),
        (10022, 'Zoologist', 'active', '2131'),
        (9441, 'Business Owner', 'active', '1219'),
        (8780, 'Engineer (biomedical) ', 'active', '2149'),
        (8605, 'Engineer (chemical)', 'active', '2145'),
        (8627, 'Engineer (civil)', 'active', '2142'),
        (8628, 'Engineer (electrical)', 'active', '2151'),
        (8781, 'Engineer (electronic) ', 'active', '2152'),
        (8782, 'Engineer (industrial) ', 'active', '2141'),
        (8629, 'Engineer (mechanical)', 'active', '2144'),
        (8785, 'Engineer (other)', 'active', '214'),
        (8783, 'Engineer (petroleum)', 'active', '2145'),
        (8630, 'Farmer (crop and vegetable)', 'active', '611'),
        (8635, 'Fishery, hunting or trapping labourer', 'active', '622'),
        (8638, 'Forestry worker and logger', 'active', '621'),
        (9443, 'Freelance', 'inactive', '9622'),
        (8643, 'Graphic designer', 'active', '2166'),
        (8646, 'Healthcare professional (medical doctor)', 'active', '2211'),
        (8647, 'Healthcare professional (midwife)', 'active', '3222'),
        (8648, 'Healthcare professional (nurse)', 'active', '222'),
        (8650, 'Healthcare professional (other)', 'active', '226'),
        (8651, 'Healthcare professional (physical therapist)', 'active', '2264'),
        (9436, 'Housewife', 'active', '5152'),
        (8653, 'Humanitarian worker', 'inactive', '1114'),
        (8786, 'Human resource manager', 'active', '1212'),
        (8655, 'Insurance', 'active', '3321'),
        (8656, 'Judge', 'active', '2612'),
        (8657, 'Lawyer', 'active', '2611'),
        (8539, 'Machine Operator (cement/other minerals)', 'active', '811'),
        (8661, 'Machine Operator (fur & leather)', 'active', '815'),
        (8662, 'Machine Operator (meat and fish)', 'active', '816'),
        (8663, 'Manufacturing labourer', 'active', '9329'),
        (8664, 'Marketing professional', 'active', '2431'),
        (8665, 'Mathematician or related professional', 'active', '2120'),
        (8544, 'Mechanic (machinery)', 'active', '723'),
        (8545, 'Metal worker', 'active', '7213'),
        (8669, 'Meteorologist', 'active', '2112'),
        (8546, 'Mining and quarrying worker', 'active', '811'),
        (8670, 'Mining or quarry worker', 'active', '811'),
        (8547, 'Motor vehicle driver (cab, truck, or other)', 'active', '83'),
        (8758, 'Nursing', 'active', '222'),
        (8673, 'Other legal professional', 'active', '261'),
        (8674, 'Performing artist (musician, actor/actreess, etc.)', 'active', '265'),
        (8675, 'Personal and Protective Service Worker', 'active', '5414'),
        (8552, 'Pharmaceutical assistant', 'active', '3213'),
        (8677, 'Pharmacist', 'active', '2262'),
        (8679, 'Photographer', 'active', '3431'),
        (8680, 'Photographer, image or sound equipment operator', 'active', '352'),
        (8681, 'Plasterer/painter/varnisher', 'active', '712'),
        (8556, 'Plumber or pipe fitter', 'active', '7126'),
        (8683, 'Police officer', 'active', '5412'),
        (8684, 'Poultry producer', 'active', '6122'),
        (8685, 'Professor or Lecturer', 'active', '231'),
        (8686, 'Psychologist', 'active', '2634'),
        (8687, 'Religious professional', 'active', '3413'),
        (8688, 'Robotic Specialist', 'active', '3119'),
        (8689, 'Safety, health & quality inspector', 'active', '3257'),
        (8690, 'Sales professional', 'active', '332'),
        (8691, 'Sales representatives/merchant/trader', 'active', '3322'),
        (8692, 'School inspector', 'active', '2351'),
        (8693, 'Scribe or related worker', 'active', '441'),
        (8694, 'Secretary', 'active', '4120'),
        (8565, 'Sewer, embroiderer or related', 'active', '753'),
        (8696, 'Ship and aircraft controller or technician', 'active', '315'),
        (8697, 'Social scientist', 'active', '2632'),
        (8567, 'Social work professional', 'active', '2635'),
        (8784, 'Software engineer', 'active', '2512'),
        (8700, 'Special education teacher', 'active', '2352'),
        (8701, 'Statistician', 'active', '2120'),
        (8702, 'Street food vendor', 'active', '5212'),
        (8571, 'Tailor, dressmaker or hatter', 'active', '753'),
        (8706, 'Teller or other counter clerk', 'active', '421'),
        (8707, 'Upholsterer or related worker', 'active', '7534'),
        (8708, 'Veterinarian', 'active', '2250'),
        (8574, 'Waiter, waitress, bartender, restaurant worker', 'active', '513'),
        (8710, 'Welder or flamecutter', 'active', '721'),
        (8711, 'Woodworker', 'active', '752'),
        (8712, 'Writer', 'active', '2641'),
        (8649, 'Optometrist', 'active', '2267'),
        (8672, 'Other Craft and Related Workers', 'active', '754'),
        (8642, 'Geologist', 'active', '2114'),
        (10023, 'Inspector, fire', 'active', '3112'),
        (10037, 'Audiologist and Speech Therapists', 'active', '2266');

insert into public.survey_type (id, name, status)
values  (1, 'Information Session', 'active'),
        (2, 'Community centre posting - flyers', 'active'),
        (4, 'Facebook', 'active'),
        (6, 'Outreach worker', 'active'),
        (7, 'NGO', 'active'),
        (8, 'Other', 'active'),
        (9, 'UNHCR', 'active'),
        (10, 'US-Afghan', 'inactive'),
        (12, 'ULYP', 'inactive'),
        (13, 'Techfugees', 'inactive'),
        (11, 'Al Ghurair Foundation', 'inactive'),
        (3, 'From a friend', 'inactive'),
        (14, 'Friend or colleague referral', 'active'),
        (5, 'Facebook - through an organisation', 'inactive'),
        (15, 'Online Google search', 'active'),
        (16, 'Instagram', 'active'),
        (17, 'LinkedIn', 'active'),
        (18, 'X', 'active'),
        (19, 'WhatsApp', 'active'),
        (20, 'YouTube', 'active'),
        (21, 'University or school referral', 'active'),
        (22, 'Employer referral', 'active'),
        (23, 'Event or webinar', 'active');
