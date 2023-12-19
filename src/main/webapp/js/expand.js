/*
    Copyright (c) 2015-2023 tracetronic GmbH

    SPDX-License-Identifier: BSD-3-Clause
*/

(function($) {
    $(function() {
        function expandAll(parent) {
            var children = getChildren(parent);
            var toggler = $(parent).children('td')[0];
            if ($(toggler).hasClass('close')) {
               $(toggler).removeClass('close').addClass('open');
            }
            $.each(children, function() {
                $(this).show();
            })
        }
        function collapseAll(parent) {
            var children = getChildren(parent);
            var toggler = $(parent).children('td')[0];
            if ($(toggler).hasClass('open')) {
               $(toggler).removeClass('open').addClass('close');
            }
            $.each(children, function() {
                $(this).hide();
            })
        }
        function getChildren($row) {
            var children = [];
            var id = $row.prop('id');
            while($row.next().hasClass('child-' + id)) {
                 children.push($row.next());
                 $row = $row.next();
            }
            return children;
        }
        $('.expander').click(function() {
            if ($(this).text() == '[Expand All]') {
                $(this).next().find('.parent').each(function() {
                    expandAll($(this));
                })
                $(this).text('[Collapse All]');
            } else if ($(this).text() == '[Alles erweitern]') {
                $(this).next().find('.parent').each(function() {
                    expandAll($(this));
                })
                $(this).text('[Alles reduzieren]');
            } else if ($(this).text() == '[Collapse All]') {
                $(this).next().find('.parent').each(function() {
                    collapseAll($(this));
                })
                $(this).text('[Expand All]');
            } else if ($(this).text() == '[Alles reduzieren]') {
                $(this).next().find('.parent').each(function() {
                    collapseAll($(this));
                })
                $(this).text('[Alles erweitern]');
            }
            return false;
        });
        $('.parent').click(function() {
            var children = getChildren($(this));
            var toggler = $(this).children('td');
            if ($(toggler).hasClass('close')) {
               $(toggler).removeClass('close').addClass('open');
            } else {
               $(toggler).removeClass('open').addClass('close');
            }
            $.each(children, function() {
                $(this).toggle();
            })
        });
        $('.parent').each(function() {
            var children = getChildren($(this));
            var toggler = $(this).children('td');
            if (!($(toggler).hasClass('FAILED') || $(toggler).hasClass('ERROR'))) {
                $(this).click();
            }
        });
    });
})(jQuery)
