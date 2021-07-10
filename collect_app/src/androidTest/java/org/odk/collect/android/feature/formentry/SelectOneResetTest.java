package org.odk.collect.android.feature.formentry;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.FormHierarchyPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collections;

import timber.log.Timber;

import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Appearance.Minimal;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Appearance.Plain;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.A;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.B;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.C;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.D;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.Block.E;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.ItemsetType.FastExternal;
import static org.odk.collect.android.feature.formentry.SelectOneResetTest.ItemsetType.Internal;

public class SelectOneResetTest {

    enum ItemsetType {
        Internal,
        FastExternal
    }

    enum Appearance {
        Plain,
        Autocomplete,
        Minimal,
        MinimalAutocomplete;

        public boolean isMinimal() {
            return this == Minimal || this == MinimalAutocomplete;
        }
    }

    enum SectionVariant {
        Internal_Plain(Internal, Plain),
        Internal_Minimal(Internal, Minimal),
        FastExternal_Plain(FastExternal, Plain),
        FastExternal_Minimal(FastExternal, Minimal),
        /*Internal_MinimalAutocomplete(Internal, MinimalAutocomplete),
        FastExternal_MinimalAutocomplete(FastExternal, MinimalAutocomplete),
        Internal_Autocomplete(Internal, Autocomplete),
        FastExternal_Autocomplete(FastExternal, Autocomplete)*/;

        final ItemsetType itemsetType;
        final Appearance appearance;

        SectionVariant(ItemsetType itemsetType, Appearance appearance) {
            this.itemsetType = itemsetType;
            this.appearance = appearance;
        }
    }

    enum Block {
        A, B, C, D, E;

        @NotNull String groupLabel(@NotNull SectionVariant variant) {
            return "group_" + name() + "-" + variant.ordinal();
        }

        @NotNull String stateLabel(@NotNull SectionVariant variant) {
            return "state_" + name() + "-" + variant.ordinal();
        }

        @NotNull String countyLabel(@NotNull SectionVariant variant) {
            return "county_" + name() + "-" + variant.ordinal();
        }

        @NotNull String cityLabel(@NotNull SectionVariant variant) {
            return "city_" + name() + "-" + variant.ordinal();
        }

        @NotNull String wardLabel(@NotNull SectionVariant variant) {
            return "ward_" + name() + "-" + variant.ordinal();
        }

        @NotNull String showWardLabel(@NotNull SectionVariant variant) {
            return "show-ward_" + name() + "-" + variant.ordinal();
        }

    }

    static final String TEXT_NO = "no";
    static final String TEXT_YES = "yes";
    static final String TEXT_SELECT_ANSWER = "Select Answer";
    static final String TEXT_HARLINGEN = "Harlingen";
    static final String TEXT_BROWNSVILLE = "Brownsville";
    static final String TEXT_TEXAS = "Texas";
    static final String TEXT_WASHINGTON = "Washington";
    static final String TEXT_NORTH = "North";
    static final String TEXT_SOUTH = "South";
    static final String TEXT_WEST = "West";
    static final String TEXT_EAST = "East";
    static final String TEXT_FORM = "selectOneReset";

    public CollectTestRule rule = new CollectTestRule();
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(TEXT_FORM + ".xml",
                    Collections.singletonList(TEXT_FORM + "-media/itemsets.csv")))
            .around(rule);

    @Test
    public void testAllVariants() {
        FormHierarchyPage hierarchy = new MainMenuPage()
                .startBlankForm(TEXT_FORM)
                .clickGoToArrow();
        for (SectionVariant variant : SectionVariant.values()) {
            int ordinal = variant.ordinal();
            Timber.i("testing " + variant + "=" + ordinal);
            testBlockABC(A, variant, hierarchy)
                    .clickOnGroup(B.groupLabel(variant));
            testBlockABC(B, variant, hierarchy)
                    .clickOnGroup(C.groupLabel(variant));
            testBlockABC(C, variant, hierarchy)
                    .clickGoUpIcon()
                    .clickGoUpIcon()
                    .clickOnGroup(D.groupLabel(variant));
            testBlocksDE(variant, hierarchy)
                    .clickGoUpIcon();
            Timber.i("passed " + variant + "=" + ordinal);
        }
    }

    @NotNull FormHierarchyPage testBlockABC(Block block, SectionVariant variant, @NotNull FormHierarchyPage hierarchy) {
        Timber.i(newBlockMsg(block, variant));
        String showWardLabel = block.showWardLabel(variant);
        String cityLabel = block.cityLabel(variant);
        String wardLabel = block.wardLabel(variant);
        String stateLabel = block.stateLabel(variant);
        boolean minimal = variant.appearance.isMinimal();
        FormEntryPage entry = hierarchy
                .clickOnQuestion(showWardLabel)
                .clickOnText(TEXT_NO)
                .swipeToPreviousQuestion(cityLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_HARLINGEN)
                .swipeToNextQuestion(showWardLabel)
                .clickOnText(TEXT_YES)
                .clickGoToArrow();
        if (block != A && variant.itemsetType != FastExternal) {
            //BC1h
            hierarchy.assertTextDoesNotExist(TEXT_NORTH);
        }
        //ABC1e
        hierarchy.clickOnQuestion(wardLabel)
                .assertTextDoesNotExist();
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_EAST)
                .swipeToPreviousQuestion(showWardLabel)
                .swipeToPreviousQuestion(cityLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_BROWNSVILLE)
                .clickGoToArrow();
        if (block != A && variant.itemsetType != FastExternal) {
            //BC2h
            hierarchy.assertTextDoesNotExist(TEXT_EAST);
        }
        //ABC2e
        hierarchy.clickOnQuestion(wardLabel)
                .assertTextDoesNotExist();
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_NORTH)
                .clickGoToArrow()
                .clickOnQuestion(stateLabel);
        if (minimal) {
            entry.openSelectMinimalDialog();
        }
        entry.clickOnText(TEXT_WASHINGTON);
        if (block != A) {
            //BC3h
            entry.clickGoToArrow()
                    .assertText(TEXT_WASHINGTON, TEXT_YES)
                    .assertTextDoesNotExist(TEXT_NORTH)
                    .clickOnQuestion(stateLabel);
        }
        //ABC3e
        entry.swipeToNextQuestion(block.countyLabel(variant))
                .assertTextDoesNotExist()
                .swipeToNextQuestion(block.cityLabel(variant))
                .assertTextDoesNotExist()
                .swipeToNextQuestion(block.showWardLabel(variant))
                .swipeToNextQuestion(block.wardLabel(variant))
                .assertTextDoesNotExist()
                .clickGoToArrow();

        return hierarchy;
    }

    FormHierarchyPage testBlocksDE(SectionVariant variant, FormHierarchyPage hierarchy) {
        Block block = D;
        Timber.i(newBlockMsg(block, variant));
        String wardLabel = block.wardLabel(variant);
        FormEntryPage entry = hierarchy.clickOnQuestion(wardLabel);
        if (variant.appearance.isMinimal()) {
            entry.clickOnText(TEXT_NO, 0)
                    .clickOnText(TEXT_BROWNSVILLE, 0)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_YES, 0);
            //DE1
            entry.clickOnText(TEXT_SELECT_ANSWER)
                    .clickOnText(TEXT_EAST)
                    .scrollToAndClickText(TEXT_HARLINGEN, 0)
                    .clickOnText(TEXT_BROWNSVILLE)
                    //DE2
                    .clickOnText(TEXT_SELECT_ANSWER)
                    .clickOnText(TEXT_NORTH)
                    .scrollToAndClickText(TEXT_TEXAS, 0)
                    .clickOnText(TEXT_WASHINGTON);
        } else {
            entry.scrollToAndClickText(TEXT_NO, 0)
                    .clickOnText(TEXT_HARLINGEN, 0)
                    .scrollToAndClickText(TEXT_YES, 0)
                    .scrollToText(TEXT_WEST, 0)
                    //DE1
                    .assertTextIsNotChecked(TEXT_WEST, 0)
                    .assertTextIsNotChecked(TEXT_EAST, 0)
                    .clickOnText(TEXT_EAST, 0)
                    .clickOnText(TEXT_BROWNSVILLE, 0)
                    .scrollToText(TEXT_SOUTH, 0)
                    //DE2
                    .assertTextIsNotChecked(TEXT_SOUTH, 0)
                    .assertTextIsNotChecked(TEXT_NORTH, 0)
                    .clickOnText(TEXT_NORTH, 0)
                    .scrollToAndClickText(TEXT_WASHINGTON, 0);
        }
        entry.clickGoToArrow()
                //DE3
                .assertText(TEXT_WASHINGTON, TEXT_YES)
                .assertTextDoesNotExist(TEXT_NORTH);

        block = E;
        Timber.i(newBlockMsg(block, variant));
        String groupLabel = block.groupLabel(variant);
        wardLabel = block.wardLabel(variant);
        entry = hierarchy.clickOnGroup(groupLabel)
                .clickOnQuestion(wardLabel)
                .scrollToText(wardLabel, 0)
                .clickOnText(TEXT_NO, 1);
        if (variant.appearance.isMinimal()) {
            entry.scrollToAndClickText(TEXT_BROWNSVILLE, 0)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_YES, 1)
                    //DE1
                    .scrollToAndClickText(TEXT_SELECT_ANSWER, 3)
                    .clickOnText(TEXT_EAST)
                    .clickOnText(TEXT_HARLINGEN)
                    .clickOnText(TEXT_BROWNSVILLE)
                    //DE2
                    .scrollToAndClickText(TEXT_SELECT_ANSWER, 3)
                    .clickOnText(TEXT_NORTH)
                    .clickOnText(TEXT_TEXAS)
                    .clickOnText(TEXT_WASHINGTON);
        } else {
            entry.clickOnText(TEXT_HARLINGEN)
                    .scrollToAndClickText(TEXT_YES, 1)
                    .scrollToText(TEXT_WEST, 0)
                    //DE1
                    .assertTextIsNotChecked(TEXT_WEST, 0)
                    .assertTextIsNotChecked(TEXT_EAST, 0)
                    .clickOnText(TEXT_EAST)
                    .clickOnText(TEXT_BROWNSVILLE)
                    .scrollToText(TEXT_SOUTH, 0)
                    //DE2
                    .assertTextIsNotChecked(TEXT_SOUTH, 0)
                    .assertTextIsNotChecked(TEXT_NORTH, 0)
                    .clickOnText(TEXT_NORTH)
                    .scrollToAndClickText(TEXT_WASHINGTON, 1);
        }
        entry.clickGoToArrow()
                .clickOnGroup(groupLabel)
                //DE3
                .assertText(TEXT_WASHINGTON, TEXT_YES)
                .assertTextDoesNotExist(TEXT_NORTH)
                .clickGoUpIcon();

        return hierarchy;
    }

    @NotNull
    private String newBlockMsg(@NotNull Block block, @NotNull SectionVariant variant) {
        return "Block " + block.name() + "-" + variant.ordinal();
    }
}
