/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets.items;

import android.content.Context;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.R;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.fastexternalitemset.ItemsetDao;
import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter;
import org.odk.collect.android.fastexternalitemset.XPathParseTool;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.SelectOneWidgetUtils;
import org.odk.collect.android.widgets.QuestionWidget;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemsWidget is an abstract class used by widgets containing a list of choices.
 * Those choices might be read from a form (xml file) or an external csv file and used in questions
 * like: SelectOne, SelectMultiple, Ranking.
 */
public abstract class ItemsWidget extends QuestionWidget {

    List<SelectChoice> items = new ArrayList<>();

    public ItemsWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        if (isFastExternalItemsetUsed()) {
            readFastExternalItems();
        } else if (isSearchPulldataItemsetUsed()) {
            readSearchPulldataItems();
        } else {
            items = getFormEntryPrompt().getSelectChoices();
        }
    }

    private boolean isFastExternalItemsetUsed() {
        QuestionDef questionDef = getFormEntryPrompt().getQuestion();
        return questionDef != null && questionDef.getAdditionalAttribute(null, "query") != null;
    }

    private boolean isSearchPulldataItemsetUsed() {
        return ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint()) != null;
    }

    private void readFastExternalItems() {
        try {
            items = new ItemsetDao(new ItemsetDbAdapter()).getItems(getFormEntryPrompt(), new XPathParseTool());
        } catch (FileNotFoundException e) {
            showWarning(getContext().getString(R.string.file_missing, e.getMessage()));
        } catch (XPathSyntaxException e) {
            showWarning(getContext().getString(R.string.parser_exception, e.getMessage()));
        }
    }

    private void readSearchPulldataItems() {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xpathFuncExpr = ExternalDataUtil.getSearchXPathExpression(getFormEntryPrompt().getAppearanceHint());
        try {
            items = ExternalDataUtil.populateExternalChoices(getFormEntryPrompt(), xpathFuncExpr);
        } catch (FileNotFoundException e) {
            showWarning(getContext().getString(R.string.file_missing, e.getMessage()));
        } catch (ExternalDataException e) {
            showWarning(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    public void setOnLongClickListener(OnLongClickListener l) {
    }

    protected void clearFollowingItemsetWidgets() {
        SelectOneWidgetUtils.checkFastExternalCascade(this);
    }
}
