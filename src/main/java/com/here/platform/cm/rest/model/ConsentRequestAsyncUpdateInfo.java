/*
 * HERE Consent Management API v1
 * HERE Consent Management REST API. More details can be found here: https://confluence.in.here.com/display/OLP/Neutral+Server+Consent+Management
 *
 * OpenAPI spec version: 3.0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.here.platform.cm.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * ConsentRequestAsyncUpdateInfo
 */

public class ConsentRequestAsyncUpdateInfo {
  /**
   * Specific action of Consent Request Async Update e.g. addDataSubjects
   */
  public enum ActionEnum {
    ADD_DATA_SUBJECTS("ADD_DATA_SUBJECTS"),
    
    REMOVE_ALL_VINS("REMOVE_ALL_VINS"),
    
    REMOVE_NON_APPROVED_VINS("REMOVE_NON_APPROVED_VINS");

    private String value;

    ActionEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ActionEnum fromValue(String text) {
      for (ActionEnum b : ActionEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
  }

  @JsonProperty("action")
  private ActionEnum action;

  @JsonProperty("consentRequestId")
  private String consentRequestId;

  @JsonProperty("finishedAt")
  private LocalDateTime finishedAt;

  @JsonProperty("id")
  private Long id;

  @JsonProperty("startedAt")
  private LocalDateTime startedAt;

  /**
   * Status of ConsentRequestAsyncUpdate
   */
  public enum StatusEnum {
    IN_PROGRESS("IN_PROGRESS"),
    
    FINISHED("FINISHED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + text + "'");
    }
  }

  @JsonProperty("status")
  private StatusEnum status;

  @JsonProperty("vinUpdateErrors")
  private List<VinUpdateError> vinUpdateErrors = null;

  public ConsentRequestAsyncUpdateInfo action(ActionEnum action) {
    this.action = action;
    return this;
  }

   /**
   * Specific action of Consent Request Async Update e.g. addDataSubjects
   * @return action
  **/
  @ApiModelProperty(value = "Specific action of Consent Request Async Update e.g. addDataSubjects")
  public ActionEnum getAction() {
    return action;
  }

  public void setAction(ActionEnum action) {
    this.action = action;
  }

  public ConsentRequestAsyncUpdateInfo consentRequestId(String consentRequestId) {
    this.consentRequestId = consentRequestId;
    return this;
  }

   /**
   * ConsentRequestId for which this ConsentRequestAsyncUpdate is associated with
   * @return consentRequestId
  **/
  @ApiModelProperty(value = "ConsentRequestId for which this ConsentRequestAsyncUpdate is associated with")
  public String getConsentRequestId() {
    return consentRequestId;
  }

  public void setConsentRequestId(String consentRequestId) {
    this.consentRequestId = consentRequestId;
  }

  public ConsentRequestAsyncUpdateInfo finishedAt(LocalDateTime finishedAt) {
    this.finishedAt = finishedAt;
    return this;
  }

   /**
   * Date when Consent Request Async Update was finished
   * @return finishedAt
  **/
  @ApiModelProperty(value = "Date when Consent Request Async Update was finished")
  public LocalDateTime getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(LocalDateTime finishedAt) {
    this.finishedAt = finishedAt;
  }

  public ConsentRequestAsyncUpdateInfo id(Long id) {
    this.id = id;
    return this;
  }

   /**
   * Id of Consent Request Async Update
   * @return id
  **/
  @ApiModelProperty(example = "123", value = "Id of Consent Request Async Update")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ConsentRequestAsyncUpdateInfo startedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
    return this;
  }

   /**
   * Date when Consent Request Async Update was started
   * @return startedAt
  **/
  @ApiModelProperty(value = "Date when Consent Request Async Update was started")
  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public ConsentRequestAsyncUpdateInfo status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Status of ConsentRequestAsyncUpdate
   * @return status
  **/
  @ApiModelProperty(example = "IN_PROGRESS, FINISHED", value = "Status of ConsentRequestAsyncUpdate")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public ConsentRequestAsyncUpdateInfo vinUpdateErrors(List<VinUpdateError> vinUpdateErrors) {
    this.vinUpdateErrors = vinUpdateErrors;
    return this;
  }

  public ConsentRequestAsyncUpdateInfo addVinUpdateErrorsItem(VinUpdateError vinUpdateErrorsItem) {
    if (this.vinUpdateErrors == null) {
      this.vinUpdateErrors = new ArrayList<>();
    }
    this.vinUpdateErrors.add(vinUpdateErrorsItem);
    return this;
  }

   /**
   * Info about VIN labels which were processed unsuccessfully
   * @return vinUpdateErrors
  **/
  @ApiModelProperty(value = "Info about VIN labels which were processed unsuccessfully")
  public List<VinUpdateError> getVinUpdateErrors() {
    return vinUpdateErrors;
  }

  public void setVinUpdateErrors(List<VinUpdateError> vinUpdateErrors) {
    this.vinUpdateErrors = vinUpdateErrors;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentRequestAsyncUpdateInfo consentRequestAsyncUpdateInfo = (ConsentRequestAsyncUpdateInfo) o;
    return Objects.equals(this.action, consentRequestAsyncUpdateInfo.action) &&
        Objects.equals(this.consentRequestId, consentRequestAsyncUpdateInfo.consentRequestId) &&
        Objects.equals(this.finishedAt, consentRequestAsyncUpdateInfo.finishedAt) &&
        Objects.equals(this.id, consentRequestAsyncUpdateInfo.id) &&
        Objects.equals(this.startedAt, consentRequestAsyncUpdateInfo.startedAt) &&
        Objects.equals(this.status, consentRequestAsyncUpdateInfo.status) &&
        Objects.equals(this.vinUpdateErrors, consentRequestAsyncUpdateInfo.vinUpdateErrors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, consentRequestId, finishedAt, id, startedAt, status, vinUpdateErrors);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentRequestAsyncUpdateInfo {\n");
    
    sb.append("    action: ").append(toIndentedString(action)).append("\n");
    sb.append("    consentRequestId: ").append(toIndentedString(consentRequestId)).append("\n");
    sb.append("    finishedAt: ").append(toIndentedString(finishedAt)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    startedAt: ").append(toIndentedString(startedAt)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    vinUpdateErrors: ").append(toIndentedString(vinUpdateErrors)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
