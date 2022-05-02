package com.ftpix.plugin.jellyfin.model;

public class NowPlayingItem {
    private String Type;
    private String SeriesName;
    private String Name;

    private String ParentBackdropItemId, SeriesId, Id;
    private int IndexNumber;
    private int ParentIndexNumber, ProductionYear;

    public int getProductionYear() {
        return ProductionYear;
    }

    public void setProductionYear(int productionYear) {
        ProductionYear = productionYear;
    }

    public String getSeriesId() {
        return SeriesId;
    }

    public void setSeriesId(String seriesId) {
        this.SeriesId = seriesId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public int getIndexNumber() {
        return IndexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        IndexNumber = indexNumber;
    }

    public int getParentIndexNumber() {
        return ParentIndexNumber;
    }

    public void setParentIndexNumber(int parentIndexNumber) {
        ParentIndexNumber = parentIndexNumber;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getSeriesName() {
        return SeriesName;
    }

    public void setSeriesName(String seriesName) {
        SeriesName = seriesName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getParentBackdropItemId() {
        return ParentBackdropItemId;
    }

    public void setParentBackdropItemId(String parentBackdropItemId) {
        ParentBackdropItemId = parentBackdropItemId;
    }
}
