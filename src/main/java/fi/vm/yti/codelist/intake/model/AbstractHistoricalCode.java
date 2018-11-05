package fi.vm.yti.codelist.intake.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;

import fi.vm.yti.codelist.common.dto.Views;
import io.swagger.annotations.ApiModelProperty;

@MappedSuperclass
public abstract class AbstractHistoricalCode extends AbstractCommonCode {

    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    @ApiModelProperty(dataType = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "startdate")
    @JsonView(Views.Normal.class)
    public LocalDate getStartDate() {
        if (startDate != null) {
            return startDate;
        }
        return null;
    }

    public void setStartDate(final LocalDate startDate) {
        if (startDate != null) {
            this.startDate = startDate;
        } else {
            this.startDate = null;
        }
    }

    @ApiModelProperty(dataType = "date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "enddate")
    @JsonView(Views.Normal.class)
    public LocalDate getEndDate() {
        if (endDate != null) {
            return endDate;
        }
        return null;
    }

    public void setEndDate(final LocalDate endDate) {
        if (endDate != null) {
            this.endDate = endDate;
        } else {
            this.endDate = null;
        }
    }

    @Column(name = "status")
    @JsonView(Views.Normal.class)
    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
