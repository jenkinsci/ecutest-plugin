/*
 *   Copyright (c) 2015-2017 TraceTronic GmbH
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this
 *        list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this
 *        list of conditions and the following disclaimer in the documentation and/or
 *        other materials provided with the distribution.
 *
 *     3. Neither the name of TraceTronic GmbH nor the names of its
 *        contributors may be used to endorse or promote products derived from
 *        this software without specific prior written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *   ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *   ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *   ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
            if (!($(toggler).hasClass('FAILED') || $(toggler).hasClass('ERROR')) || $this.hasClass('tool')) {
                $(this).click();
            }
        });
    });
})(jQuery)