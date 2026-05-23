package com.example.frolovnails.network.models.response;

import java.util.List;

public class ScheduleBlocksResponse {
    private List<ScheduleBlock> blocks;
    private Integer count;

    public ScheduleBlocksResponse() {}

    public List<ScheduleBlock> getBlocks() { return blocks; }
    public void setBlocks(List<ScheduleBlock> blocks) { this.blocks = blocks; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}