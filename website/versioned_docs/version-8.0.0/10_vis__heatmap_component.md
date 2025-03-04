---
title: Heatmap
sidebar_label: Heatmap
copyright: (C) 2007-2018 GoodData Corporation
id: version-8.0.0-heatmap_component
original_id: heatmap_component
---
A **heatmap** represents data as a matrix where individual values are represented as colors. Heatmaps can help you discover trends and understand complex datasets.

![Heatmap Component](assets/heatmap.png "Heatmap Component")

## Structure

```jsx
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import { Heatmap } from "@gooddata/sdk-ui-charts";

<Heatmap
    measure={<measure>}
    rows={<attribute>}
    columns={<attribute>}
    config={<chart-config>}
    …
/>
```
The following example shows the supported `config` structure with sample values. For the descriptions of the individual options, see [ChartConfig](15_props__chart_config.md).

```javascript
{
    xaxis: {
        visible: true,
        labelsEnabled: true,
        rotation: "auto"
    },
    yaxis: {
        visible: true,
        labelsEnabled: true,
        rotation: "auto"
    },
    legend: {
        enabled: true,
        position: "top"
    },
    dataLabels: {
        visible: "auto"
    },
    separators: {
        thousand: ",",
        decimal: "."
    }
}
```

## Example

```jsx
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import { Heatmap } from "@gooddata/sdk-ui-charts";

import * as Ldm from "./ldm/full";

<div style={{ height: 300 }} className="s-heat-map">
    <Heatmap
        measure={Ldm.$TotalSales}
        rows={Ldm.LocationState}
        columns={Ldm.MenuCategory}
        onLoadingChanged={this.onLoadingChanged}
        onError={this.onError}
    />
</div>
```

## Properties

| Name | Required? | Type | Description |
| :--- | :--- | :--- | :--- |
| measure | true | [IMeasure](50_custom__execution.md#measure) | The measure definition |
| rows | false | [IAttribute](50_custom__execution.md#attribute) | The attribute definition |
| columns | false | [IAttribute](50_custom__execution.md#attribute) | The attribute definition |
| filters | false | [IFilter[]](30_tips__filter_visual_components.md) | An array of filter definitions |
| config | false | [IChartConfig](15_props__chart_config.md) | The chart configuration object |
| locale | false | string | The localization of the chart. Defaults to `en-US`. For other languages, see the [full list of available localizations](https://github.com/gooddata/gooddata-ui-sdk/tree/master/libs/sdk-ui/src/base/localization/bundles). |
| drillableItems | false | [IDrillableItem[]](15_props__drillable_item.md)  | An array of points and attribute values to be drillable |
| ErrorComponent | false | Component | A component to be rendered if this component is in error state (see [ErrorComponent](15_props__error_component.md)) |
| LoadingComponent | false | Component | A component to be rendered if this component is in loading state (see [LoadingComponent](15_props__loading_component.md)) |
| onError | false | Function | A callback when the component updates its error state |
| onExportReady | false | Function | A callback when the component is ready for exporting its data |
| onLoadingChanged | false | Function | A callback when the component updates its loading state |
| onDrill | false | Function | A callback when a drill is triggered on the component |
