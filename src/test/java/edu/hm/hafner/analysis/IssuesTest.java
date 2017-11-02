package edu.hm.hafner.analysis;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import static edu.hm.hafner.analysis.assertj.Assertions.assertThat;
import static edu.hm.hafner.analysis.assertj.SoftAssertions.*;
import static java.util.Arrays.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Unit tests for {@link Issues}.
 *
 * @author Marcel Binder
 */
public class IssuesTest {
    private static final Issue ISSUE_1 = new IssueBuilder()
            .setMessage("issue-1")
            .setFileName("file-1")
            .setPriority(Priority.HIGH)
            .build();
    private static final Issue ISSUE_2 = new IssueBuilder().setMessage("issue-2").setFileName("file-1").build();
    private static final Issue ISSUE_3 = new IssueBuilder().setMessage("issue-3").setFileName("file-1").build();
    private static final Issue ISSUE_4 = new IssueBuilder()
            .setMessage("issue-4")
            .setFileName("file-2")
            .setPriority(Priority.LOW)
            .build();
    private static final Issue ISSUE_5 = new IssueBuilder()
            .setMessage("issue-5")
            .setFileName("file-2")
            .setPriority(Priority.LOW)
            .build();
    private static final Issue ISSUE_6 = new IssueBuilder()
            .setMessage("issue-6")
            .setFileName("file-3")
            .setPriority(Priority.LOW)
            .build();

    @Test
    public void testEmptyIssues() {
        Issues issues = new Issues();

        assertThat(issues).hasSize(0);
    }

    @Test
    public void testAddMultipleIssuesOneByOne() {
        Issues issues = new Issues();

        assertThat(issues.add(ISSUE_1)).isEqualTo(ISSUE_1);
        assertThat(issues.add(ISSUE_2)).isEqualTo(ISSUE_2);
        assertThat(issues.add(ISSUE_3)).isEqualTo(ISSUE_3);
        assertThat(issues.add(ISSUE_4)).isEqualTo(ISSUE_4);
        assertThat(issues.add(ISSUE_5)).isEqualTo(ISSUE_5);
        assertThat(issues.add(ISSUE_6)).isEqualTo(ISSUE_6);

        assertAllIssuesAdded(issues);
    }

    @Test
    public void testAddMultipleIssuesToEmpty() {
        Issues issues = new Issues();
        List<Issue> issueList = asList(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6);

        assertThat(issues.addAll(issueList)).isEqualTo(issueList);

        assertAllIssuesAdded(issues);
    }

    @Test
    public void testAddMultipleIssuesToNonEmpty() {
        Issues issues = new Issues();
        issues.add(ISSUE_1);

        issues.addAll(asList(ISSUE_2, ISSUE_3));
        issues.addAll(asList(ISSUE_4, ISSUE_5, ISSUE_6));

        assertAllIssuesAdded(issues);
    }

    private void assertAllIssuesAdded(final Issues issues) {
        assertSoftly(softly -> {
            softly.assertThat(issues)
                  .hasSize(6)
                  .hasNumberOfFiles(3)
                  .hasHighPrioritySize(1)
                  .hasNormalPrioritySize(2)
                  .hasLowPrioritySize(3);
            softly.assertThat(issues.getFiles()).containsExactly("file-1", "file-2", "file-3");
            softly.assertThat((Iterable<Issue>) issues)
                  .containsExactly(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6);
            softly.assertThat(issues.all())
                  .containsExactly(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6);
        });
    }

    @Test
    public void testAddDuplicate() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_1));

        assertThat(issues.all()).contains(ISSUE_1, ISSUE_1);
        assertThat(issues)
                .hasSize(2)
                .hasNumberOfFiles(1)
                .hasLowPrioritySize(0)
                .hasNormalPrioritySize(0)
                .hasHighPrioritySize(2);
    }

    @Test
    public void testRemove() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6));

        Issue removed = issues.remove(ISSUE_1.getId());

        assertThat(removed).isEqualTo(ISSUE_1);
        assertThat(issues.all()).containsOnly(ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6);
        assertThat((Iterable<Issue>) issues).containsOnly(ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6);
        assertThat(issues).hasSize(5);
        assertThat(issues).hasNumberOfFiles(3);
        assertThat(issues).hasHighPrioritySize(0);
        assertThat(issues).hasNormalPrioritySize(2);
        assertThat(issues).hasLowPrioritySize(3);
    }

    @Test
    public void testRemoveNotExisting() {
        Issues issues = new Issues();

        assertThatThrownBy(() -> issues.remove(ISSUE_1.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasNoCause()
                .hasMessageContaining(ISSUE_1.getId().toString());
    }

    @Test
    public void testRemoveDuplicate() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_1));

        issues.remove(ISSUE_1.getId());

        assertThat(issues.all()).containsOnly(ISSUE_1);
        assertThat(issues).hasSize(1);
        assertThat(issues).hasNumberOfFiles(1);
        assertThat(issues).hasHighPrioritySize(1);
    }

    @Test
    public void testFindByIdSingleIssue() {
        Issues issues = new Issues();
        issues.addAll(ImmutableList.of(ISSUE_1));

        Issue found = issues.findById(ISSUE_1.getId());

        assertThat(found).isEqualTo(ISSUE_1);
    }

    @Test
    public void testFindByIdSingleIssueNotExisting() {
        Issues issues = new Issues();
        issues.addAll(ImmutableList.of(ISSUE_1));

        assertThatThrownBy(() -> issues.findById(ISSUE_2.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasNoCause()
                .hasMessageContaining(ISSUE_2.getId().toString());
    }

    @Test
    public void testFindByIdMultipleIssues() {
        Issues issues = new Issues();
        issues.addAll(ImmutableList.of(ISSUE_1, ISSUE_2));

        Issue found = issues.findById(ISSUE_2.getId());

        assertThat(found).isEqualTo(ISSUE_2);
    }

    @Test
    public void testFindByIdMultipleIssuesNotExisting() {
        Issues issues = new Issues();
        issues.addAll(ImmutableList.of(ISSUE_1, ISSUE_2));

        assertThatThrownBy(() -> issues.findById(ISSUE_3.getId()))
                .isInstanceOf(NoSuchElementException.class)
                .hasNoCause()
                .hasMessageContaining(ISSUE_3.getId().toString());
    }

    @Test
    public void testFindNoneByProperty() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));

        ImmutableList<Issue> found = issues.findByProperty(issue -> Objects.equals(issue.getPriority(), Priority.LOW));

        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPropertyResultImmutable() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));
        ImmutableList<Issue> found = issues.findByProperty(issue -> Objects.equals(issue.getPriority(), Priority.HIGH));

        //noinspection deprecation
        assertThatThrownBy(() -> found.add(ISSUE_4))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasNoCause();
    }

    @Test
    public void testIterator() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));

        assertThat((Iterable<Issue>) issues).containsExactly(ISSUE_1, ISSUE_2, ISSUE_3);
    }

    @Test
    public void testGet() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));

        assertThat(issues.get(0)).isEqualTo(ISSUE_1);
        assertThat(issues.get(1)).isEqualTo(ISSUE_2);
        assertThat(issues.get(2)).isEqualTo(ISSUE_3);
        assertThatThrownBy(() -> issues.get(-1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasNoCause()
                .hasMessageContaining("-1");
        assertThatThrownBy(() -> issues.get(3))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasNoCause()
                .hasMessageContaining("3");
    }

    @Test
    public void testGetFiles() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6));

        assertThat(issues.getFiles()).contains("file-1", "file-1", "file-3");
    }

    @Test
    public void testToString() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));

        assertThat(issues.toString()).contains("3");
    }

    @Test
    public void testGetProperties() {
        Issues issues = new Issues();
        issues.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4, ISSUE_5, ISSUE_6));

        SortedSet<String> properties = issues.getProperties(issue -> issue.getMessage());

        assertThat(properties)
                .contains(ISSUE_1.getMessage())
                .contains(ISSUE_2.getMessage())
                .contains(ISSUE_3.getMessage());
    }

    @Test
    public void testCopy() {
        Issues original = new Issues();
        original.addAll(asList(ISSUE_1, ISSUE_2, ISSUE_3));

        Issues copy = original.copy();

        assertThat(copy).isNotSameAs(original);
        assertThat(copy.all()).contains(ISSUE_1, ISSUE_2, ISSUE_3);

        copy.add(ISSUE_4);
        assertThat(original.all()).contains(ISSUE_1, ISSUE_2, ISSUE_3);
        assertThat(copy.all()).contains(ISSUE_1, ISSUE_2, ISSUE_3, ISSUE_4);
    }
}