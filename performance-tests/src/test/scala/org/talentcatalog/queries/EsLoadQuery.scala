/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License 
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.talentcatalog.queries

import io.gatling.core.Predef._
import ru.tinkoff.load.jdbc.Predef._
import ru.tinkoff.load.jdbc.actions.actions._

object EsLoadQuery {

  val esLoadPagedQueryName: String = "ES Load Paged Query"

  val esLoadCountQueryName: String = "ES Load Count Query"

  val esLoadPaged: String =
    """
      |select
      |        candidate0_.id as id1_1_,
      |        candidate0_.created_by as create132_1_,
      |        candidate0_.created_date as created_2_1_,
      |        candidate0_.updated_by as update133_1_,
      |        candidate0_.updated_date as updated_3_1_,
      |        candidate0_.additional_info as addition4_1_,
      |        candidate0_.address1 as address5_1_,
      |        candidate0_.asylum_year as asylum_y6_1_,
      |        candidate0_.avail_immediate as avail_im7_1_,
      |        candidate0_.avail_immediate_job_ops as avail_im8_1_,
      |        candidate0_.avail_immediate_notes as avail_im9_1_,
      |        candidate0_.avail_immediate_reason as avail_i10_1_,
      |        candidate0_.birth_country_id as birth_134_1_,
      |        candidate0_.can_drive as can_dri11_1_,
      |        candidate0_.candidate_message as candida12_1_,
      |        candidate0_.candidate_number as candida13_1_,
      |        candidate0_.city as city14_1_,
      |        candidate0_.conflict as conflic15_1_,
      |        candidate0_.conflict_notes as conflic16_1_,
      |        candidate0_.country_id as countr135_1_,
      |        candidate0_.covid_vaccinated as covid_v17_1_,
      |        candidate0_.covid_vaccinated_date as covid_v18_1_,
      |        candidate0_.covid_vaccinated_status as covid_v19_1_,
      |        candidate0_.covid_vaccine_name as covid_v20_1_,
      |        candidate0_.covid_vaccine_notes as covid_v21_1_,
      |        candidate0_.crime_convict as crime_c22_1_,
      |        candidate0_.crime_convict_notes as crime_c23_1_,
      |        candidate0_.dest_limit as dest_li24_1_,
      |        candidate0_.dest_limit_notes as dest_li25_1_,
      |        candidate0_.dob as dob26_1_,
      |        candidate0_.driving_license as driving27_1_,
      |        candidate0_.driving_license_country_id as drivin136_1_,
      |        candidate0_.driving_license_exp as driving28_1_,
      |        candidate0_.external_id as externa29_1_,
      |        candidate0_.external_id_source as externa30_1_,
      |        candidate0_.family_move as family_31_1_,
      |        candidate0_.family_move_notes as family_32_1_,
      |        candidate0_.folderlink as folderl33_1_,
      |        candidate0_.folderlink_address as folderl34_1_,
      |        candidate0_.folderlink_character as folderl35_1_,
      |        candidate0_.folderlink_employer as folderl36_1_,
      |        candidate0_.folderlink_engagement as folderl37_1_,
      |        candidate0_.folderlink_experience as folderl38_1_,
      |        candidate0_.folderlink_family as folderl39_1_,
      |        candidate0_.folderlink_identity as folderl40_1_,
      |        candidate0_.folderlink_immigration as folderl41_1_,
      |        candidate0_.folderlink_language as folderl42_1_,
      |        candidate0_.folderlink_medical as folderl43_1_,
      |        candidate0_.folderlink_qualification as folderl44_1_,
      |        candidate0_.folderlink_registration as folderl45_1_,
      |        candidate0_.gender as gender46_1_,
      |        candidate0_.health_issues as health_47_1_,
      |        candidate0_.health_issues_notes as health_48_1_,
      |        candidate0_.home_location as home_lo49_1_,
      |        candidate0_.host_challenges as host_ch50_1_,
      |        candidate0_.host_entry_legally as host_en51_1_,
      |        candidate0_.host_entry_legally_notes as host_en52_1_,
      |        candidate0_.host_entry_year as host_en53_1_,
      |        candidate0_.host_entry_year_notes as host_en54_1_,
      |        candidate0_.ielts_score as ielts_s55_1_,
      |        candidate0_.int_recruit_other as int_rec56_1_,
      |        candidate0_.int_recruit_reasons as int_rec57_1_,
      |        candidate0_.int_recruit_rural as int_rec58_1_,
      |        candidate0_.int_recruit_rural_notes as int_rec59_1_,
      |        candidate0_.lang_assessment as lang_as60_1_,
      |        candidate0_.lang_assessment_score as lang_as61_1_,
      |        candidate0_.left_home_notes as left_ho62_1_,
      |        candidate0_.left_home_reasons as left_ho63_1_,
      |        candidate0_.linked_in_link as linked_64_1_,
      |        candidate0_.marital_status as marital65_1_,
      |        candidate0_.marital_status_notes as marital66_1_,
      |        candidate0_.max_education_level_id as max_ed137_1_,
      |        candidate0_.media_willingness as media_w67_1_,
      |        candidate0_.migration_education_major_id as migrat138_1_,
      |        candidate0_.migration_nationality as migrati68_1_,
      |        candidate0_.military_end as militar69_1_,
      |        candidate0_.military_notes as militar70_1_,
      |        candidate0_.military_service as militar71_1_,
      |        candidate0_.military_start as militar72_1_,
      |        candidate0_.military_wanted as militar73_1_,
      |        candidate0_.nationality_id as nation139_1_,
      |        candidate0_.partner_candidate_id as partne140_1_,
      |        candidate0_.partner_citizenship_id as partne141_1_,
      |        candidate0_.partner_edu_level_id as partne142_1_,
      |        candidate0_.partner_edu_level_notes as partner74_1_,
      |        candidate0_.partner_english as partner75_1_,
      |        candidate0_.partner_english_level_id as partne143_1_,
      |        candidate0_.partner_ielts as partner76_1_,
      |        candidate0_.partner_ielts_score as partner77_1_,
      |        candidate0_.partner_ielts_yr as partner78_1_,
      |        candidate0_.partner_occupation_id as partne144_1_,
      |        candidate0_.partner_occupation_notes as partner79_1_,
      |        candidate0_.partner_ref as partner80_1_,
      |        candidate0_.partner_registered as partner81_1_,
      |        candidate0_.phone as phone82_1_,
      |        candidate0_.rego_ip as rego_ip83_1_,
      |        candidate0_.rego_partner_param as rego_pa84_1_,
      |        candidate0_.rego_referrer_param as rego_re85_1_,
      |        candidate0_.rego_utm_campaign as rego_ut86_1_,
      |        candidate0_.rego_utm_content as rego_ut87_1_,
      |        candidate0_.rego_utm_medium as rego_ut88_1_,
      |        candidate0_.rego_utm_source as rego_ut89_1_,
      |        candidate0_.rego_utm_term as rego_ut90_1_,
      |        candidate0_.resettle_third as resettl91_1_,
      |        candidate0_.resettle_third_status as resettl92_1_,
      |        candidate0_.residence_status as residen93_1_,
      |        candidate0_.residence_status_notes as residen94_1_,
      |        candidate0_.return_home_future as return_95_1_,
      |        candidate0_.return_home_safe as return_96_1_,
      |        candidate0_.return_home_when as return_97_1_,
      |        candidate0_.returned_home as returne98_1_,
      |        candidate0_.returned_home_reason as returne99_1_,
      |        candidate0_.returned_home_reason_no as return100_1_,
      |        candidate0_.sflink as sflink101_1_,
      |        candidate0_.shareable_cv_attachment_id as sharea145_1_,
      |        candidate0_.shareable_doc_attachment_id as sharea146_1_,
      |        candidate0_.shareable_notes as sharea102_1_,
      |        candidate0_.state as state103_1_,
      |        candidate0_.status as status104_1_,
      |        candidate0_.survey_comment as survey105_1_,
      |        candidate0_.survey_type_id as survey147_1_,
      |        candidate0_.text_search_id as text_s106_1_,
      |        candidate0_.unhcr_consent as unhcr_107_1_,
      |        candidate0_.unhcr_file as unhcr_108_1_,
      |        candidate0_.unhcr_not_reg_status as unhcr_109_1_,
      |        candidate0_.unhcr_notes as unhcr_110_1_,
      |        candidate0_.unhcr_number as unhcr_111_1_,
      |        candidate0_.unhcr_status as unhcr_112_1_,
      |        candidate0_.unrwa_file as unrwa_113_1_,
      |        candidate0_.unrwa_not_reg_status as unrwa_114_1_,
      |        candidate0_.unrwa_notes as unrwa_115_1_,
      |        candidate0_.unrwa_number as unrwa_116_1_,
      |        candidate0_.unrwa_registered as unrwa_117_1_,
      |        candidate0_.user_id as user_i148_1_,
      |        candidate0_.videolink as videol118_1_,
      |        candidate0_.visa_issues as visa_i119_1_,
      |        candidate0_.visa_issues_notes as visa_i120_1_,
      |        candidate0_.visa_reject as visa_r121_1_,
      |        candidate0_.visa_reject_notes as visa_r122_1_,
      |        candidate0_.whatsapp as whatsa123_1_,
      |        candidate0_.work_abroad as work_a124_1_,
      |        candidate0_.work_abroad_notes as work_a125_1_,
      |        candidate0_.work_desired as work_d126_1_,
      |        candidate0_.work_desired_notes as work_d127_1_,
      |        candidate0_.work_permit as work_p128_1_,
      |        candidate0_.work_permit_desired as work_p129_1_,
      |        candidate0_.work_permit_desired_notes as work_p130_1_,
      |        candidate0_.year_of_arrival as year_o131_1_,
      |        (SELECT
      |            COUNT(cd.id)
      |        FROM
      |            candidate c
      |        inner join
      |            candidate_dependant cd
      |                on c.id = cd.candidate_id
      |        where
      |            c.id = candidate0_.id
      |        group by
      |            c.id) as formula1_
      |    from
      |        candidate candidate0_
      |    where
      |        candidate0_.status<>'deleted'
      |    order by
      |        candidate0_.id asc nulls last limit 20 offset 20
      |""".stripMargin

  val esLoadCount: String =
    """
      |select
      |        count(candidate0_.id) as col_0_0_
      |    from
      |        candidate candidate0_
      |    where
      |        candidate0_.status<>'deleted'
      |""".stripMargin

  def esLoadPagedQuery(): RawSqlActionBuilder =
    jdbc(esLoadPagedQueryName)
      .rawSql(esLoadPaged)

  def esLoadCountQuery(): RawSqlActionBuilder =
    jdbc(esLoadCountQueryName)
      .rawSql(esLoadCount)

}
