---
title: Pie Chart
sidebar_label: Pie Chart
copyright: (C) 2007-2018 GoodData Corporation
id: pie_chart_component
---

A **pie chart** shows data as proportional segments of a disc. Pie charts can be segmented by either multiple measures or an attribute.

![Pie Chart Component](assets/pie_chart.png "Pie Chart Component")

## Structure

```jsx
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import { PieChart } from "@gooddata/sdk-ui-charts";

<PieChart
    measures={<measures>}
    config={<chart-config>}
    …
/>
```

## Example

```jsx
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import { PieChart } from "@gooddata/sdk-ui-charts";
import * as Md from "./md/full";

const style = { height: 300 };

<div style={style}>
    <PieChart
        measures={[Md.$FranchiseFeesAdRoyalty, Md.$FranchiseFeesInitialFranchiseFee, Md.$FranchiseFeesOngoingRoyalty]}
    />
</div>
```

## Properties

| Name | Required? | Type | Description |
| :--- | :--- | :--- | :--- |
| measures | true | [IMeasure[]](50_custom__execution.md#measure) | An array of measure definitions |
| viewBy | false | [IAttribute](50_custom__execution.md#attribute) | The attribute definition |
| filters | false | [IFilter[]](30_tips__filter_visual_components.md) | An array of filter definitions |
| sortBy | false | [ISortItem[]](50_custom__result.md#sorting) | An array of sort definitions |
| config | false | [IChartConfig](15_props__chart_config.md) | The chart configuration object |
| locale | false | string | The localization of the chart. Defaults to `en-US`. For other languages, see the [full list of available localizations](https://github.com/gooddata/gooddata-ui-sdk/tree/master/libs/sdk-ui/src/base/localization/bundles). |
| drillableItems | false | [IDrillableItem[]](15_props__drillable_item.md) | An array of points and attribute values to be drillable |
| ErrorComponent | false | Component | A component to be rendered if this component is in error state (see [ErrorComponent](15_props__error_component.md)) |
| LoadingComponent | false | Component | A component to be rendered if this component is in loading state (see [LoadingComponent](15_props__loading_component.md)) |
| onError | false | Function | A callback when the component updates its error state |
| onExportReady | false | Function | A callback when the component is ready for exporting its data |
| onLoadingChanged | false | Function | A callback when the component updates its loading state |
| onDrill | false | Function | A callback when a drill is triggered on the component |

The following example shows the supported `config` structure with sample values. For the descriptions of the individual options, see [ChartConfig](15_props__chart_config.md).

```javascript
{
    colors: ["rgb(195, 49, 73)", "rgb(168, 194, 86)"],
    colorPalette: [{
        guid: "01",
        fill: {
            r: 195,
            g: 49,
            b: 73
        }
    }, {
        guid: "02",
        fill: {
            r: 168,
            g: 194,
            b: 86
        }
    }],
    colorMapping: [{
        predicate: (headerItem) => {
            return headerItem.measureHeaderItem && (headerItem.measureHeaderItem.localIdentifier === "m1_localIdentifier")
        },
        color: {
            type: "guid",
            value: "02"
        }
    }],
    legend: {
        enabled: true,
        position: "top",
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
