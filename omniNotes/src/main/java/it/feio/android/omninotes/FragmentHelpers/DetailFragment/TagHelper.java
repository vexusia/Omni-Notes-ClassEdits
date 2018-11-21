package it.feio.android.omninotes.FragmentHelpers.DetailFragment;

import android.graphics.Color;
import android.support.v4.util.Pair;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import it.feio.android.checklistview.models.CheckListViewItem;
import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.Tag;
import it.feio.android.omninotes.utils.Constants;
import it.feio.android.omninotes.utils.TagsHelper;

public class TagHelper {
    private final DetailFragment detailFragment;

    public TagHelper(DetailFragment detailFragment) {
        this.detailFragment = detailFragment;
    }

    /**
     * Colors tag marker in note's title and content elements
     */
    public void setTagMarkerColor(Category tag) {

        String colorsPref = detailFragment.getPrefs().getString("settings_colors_app", Constants.PREF_COLORS_APP_DEFAULT);

        // Checking preference
        if (!"disabled".equals(colorsPref)) {

            // Choosing target view depending on another preference
            ArrayList<View> target = new ArrayList<View>();
            if ("complete".equals(colorsPref)) {
                target.add(detailFragment.getTitleWrapperView());
                target.add(detailFragment.getScrollView());
            } else {
                target.add(detailFragment.getTagMarkerView());
            }

            // Coloring the target
            if (tag != null && tag.getColor() != null) {
                for (View view : target) {
                    view.setBackgroundColor(Integer.parseInt(tag.getColor()));
                }
            } else {
                for (View view : target) {
                    view.setBackgroundColor(Color.parseColor("#00000000"));
                }
            }
        }
    }

    /**
     * Add previously created tags to content
     */
    public void addTags() {
        detailFragment.setContentCursorPosition(detailFragment.getCursorIndex());

        // Retrieves all available categories
        final List<Tag> tags = TagsHelper.getAllTags();

        // If there is no tag a message will be shown
        if (tags.size() == 0) {
            detailFragment.getMainActivity().showMessage(R.string.no_tags_created, ONStyle.WARN);
            return;
        }

        final Note currentNote = new Note();
        currentNote.setTitle(detailFragment.getNoteTitle());
        currentNote.setContent(detailFragment.getNoteContent());
        Integer[] preselectedTags = TagsHelper.getPreselectedTagsArray(currentNote, tags);

        // Dialog and events creation
        MaterialDialog dialog = new MaterialDialog.Builder(detailFragment.getMainActivity())
                .title(R.string.select_tags)
                .positiveText(R.string.ok)
                .items(TagsHelper.getTagsArray(tags))
                .itemsCallbackMultiChoice(preselectedTags, (dialog1, which, text) -> {
                    dialog1.dismiss();
                    detailFragment.tagNote(tags, which, currentNote);
                    return false;
                }).build();
        dialog.show();
    }

    public void tagNote(List<Tag> tags, Integer[] selectedTags, Note note) {
        Pair<String, List<Tag>> taggingResult = TagsHelper.addTagToNote(tags, selectedTags, note);

        StringBuilder sb;
        if (this.detailFragment.noteTmp.isChecklist()) {
            CheckListViewItem mCheckListViewItem = this.detailFragment.mChecklistManager.getFocusedItemView();
            if (mCheckListViewItem != null) {
                sb = new StringBuilder(mCheckListViewItem.getText());
                sb.insert(this.detailFragment.contentCursorPosition, " " + taggingResult.first + " ");
                mCheckListViewItem.setText(sb.toString());
                mCheckListViewItem.getEditText().setSelection(this.detailFragment.contentCursorPosition + taggingResult.first.length()
                        + 1);
            } else {
                this.detailFragment.title.append(" " + taggingResult.first);
            }
        } else {
            sb = new StringBuilder(this.detailFragment.getNoteContent());
            if (this.detailFragment.content.hasFocus()) {
                sb.insert(this.detailFragment.contentCursorPosition, " " + taggingResult.first + " ");
                this.detailFragment.content.setText(sb.toString());
                this.detailFragment.content.setSelection(this.detailFragment.contentCursorPosition + taggingResult.first.length() + 1);
            } else {
                if (this.detailFragment.getNoteContent().trim().length() > 0) {
                    sb.append(System.getProperty("line.separator"))
                            .append(System.getProperty("line.separator"));
                }
                sb.append(taggingResult.first);
                this.detailFragment.content.setText(sb.toString());
            }
        }

        // Removes unchecked tags
        if (taggingResult.second.size() > 0) {
            if (this.detailFragment.noteTmp.isChecklist()) {
                this.detailFragment.toggleChecklist2(true, true);
            }
            Pair<String, String> titleAndContent = TagsHelper.removeTag(this.detailFragment.getNoteTitle(), this.detailFragment.getNoteContent(),
                    taggingResult.second);
            this.detailFragment.title.setText(titleAndContent.first);
            this.detailFragment.content.setText(titleAndContent.second);
            if (this.detailFragment.noteTmp.isChecklist()) {
                this.detailFragment.toggleChecklist2();
            }
        }
    }

}