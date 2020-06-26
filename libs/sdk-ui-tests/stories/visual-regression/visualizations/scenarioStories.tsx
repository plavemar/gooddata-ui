// (C) 2007-2018 GoodData Corporation
import { createElementCountResolver, ScreenshotReadyWrapper } from "../_infra/ScreenshotReadyWrapper";
import * as React from "react";
import allScenarios from "../../../scenarios";
import { ScenarioGroup } from "../../../src";

import "@gooddata/sdk-ui-pivot/styles/css/main.css";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-geo/styles/css/main.css";
import { withScreenshot } from "../_infra/backstopWrapper";
import { StorybookBackend } from "../_infra/backend";
import { storyGroupFor } from "./storyGroupFactory";
import groupBy = require("lodash/groupBy");
import sortBy = require("lodash/sortBy");
import { ScenarioStories } from "../_infra/storyGroups";

const DefaultWrapperStyle = { width: 800, height: 400 };

const backend = StorybookBackend();
const ScenarioGroupsByVis = Object.values(groupBy<ScenarioGroup<any>>(allScenarios, (g) => g.vis));

function simpleStory(Component: React.ComponentType, props: any, wrapperStyle: any) {
    return () => {
        return withScreenshot(
            <ScreenshotReadyWrapper resolver={createElementCountResolver(1)}>
                <div style={wrapperStyle}>
                    <Component {...props} />
                </div>
            </ScreenshotReadyWrapper>,
        );
    };
}

function groupedStory(group: ScenarioGroup<any>, wrapperStyle: any) {
    const scenarios = group.asScenarioDescAndScenario();

    return () => {
        return withScreenshot(
            <ScreenshotReadyWrapper resolver={createElementCountResolver(scenarios.length)}>
                {scenarios.map(([name, scenario], idx) => {
                    const { propsFactory, workspaceType, component: Component } = scenario;
                    const props = propsFactory(backend, workspaceType);

                    return (
                        <div key={idx}>
                            <div className="storybook-title">{name}</div>
                            <div style={wrapperStyle} className="screenshot-container">
                                <Component {...props} />
                            </div>
                        </div>
                    );
                })}
            </ScreenshotReadyWrapper>,
        );
    };
}

ScenarioGroupsByVis.forEach((groups) => {
    /*
     * Sort groups; the order in which stories for the group are created is important as that is the order
     * in which the groups appear in storybook.
     */
    const sortedGroups = sortBy(groups, (g) => g.groupNames.join("/"));

    for (const group of sortedGroups) {
        const storiesForChart = storyGroupFor(ScenarioStories, group);
        // only interested in scenarios for visual regression
        const visualOnly: ScenarioGroup<any> = group.forTestTypes("visual");

        if (visualOnly.isEmpty()) {
            // it is completely valid that some groups have no scenarios for visual regression
            continue;
        }

        // group may specify the size for its screenshots; if not there, use the default
        const wrapperStyle = group.testConfig.visual.screenshotSize || DefaultWrapperStyle;

        if (group.testConfig.visual.groupUnder) {
            // group may specify, that the scenarios should be grouped under a single story
            storiesForChart.add(group.testConfig.visual.groupUnder, groupedStory(visualOnly, wrapperStyle));
        } else {
            // otherwise there will be story-per-scenario
            const scenarios = visualOnly.asScenarioDescAndScenario();

            scenarios.forEach(([name, scenario]) => {
                const { propsFactory, workspaceType, component: Component } = scenario;
                const props = propsFactory(backend, workspaceType);

                storiesForChart.add(name, simpleStory(Component, props, wrapperStyle));
            });
        }
    }
});
