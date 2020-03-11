/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2020  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

CodeMirror.registerHelper('hint', 'jagtag', (editor, options) => {
    const words = options.words;
    const cur = editor.getCursor();
    const token = editor.getTokenAt(cur);
    const to = CodeMirror.Pos(cur.line, token.end);
    let from = to;
    let term = "";

    if (token.string && /\w/.test(token.string[token.string.length - 1])) {
        term = token.string;
        from = CodeMirror.Pos(cur.line, token.start);
    }

    const found = [];
    for (const word of words) {
        if (word.text.slice(0, term.length) === term) {
            found.push(word);
        }
    }

    if (found.length) {
        return {
            list: found,
            from: from,
            to: to
        };
    }
});

const brace = 'builtin';
const semiColon = 'bracket';
CodeMirror.defineSimpleMode('jagtag', {
    start: [
        { regex: /\d/, token: 'number' },
        { regex: /\{/, token: brace, push: 'innerTag', indent: true },
        { regex: /[:}]/, token: brace },
        { regex: /[^\{}]*/, token: 'bracket' },
    ],
    comment: [
    ],
    innerTag: [
        { regex: /\{/, token: brace, push: 'innerTag', indent: true },
        { regex: /\}/, token: brace, pop: true, dedent: true },
        { regex: /([^{}:\| ]+)/, token: 'keyword' },

        { regex: /(:|\|+)(\{)/, token: [semiColon, brace], push: 'innerTag', indent: true },
        { regex: /(:|\|+)(})/, token: [semiColon, brace], pop: true, dedent: true },
        { regex: /(:|\|+)$/, token: [semiColon] },

        { regex: /([:|]+)(\d*)?([^{}:\|]*)?/, token: [semiColon, 'number', 'string'] },
        { regex: /^(\d*)?([^{}:\|]*)?/, token: ['number', 'string'] }
    ],
    args: [
        { regex: /\{/, token: brace, push: 'innerTag', indent: true },
        { regex: /\}/, token: brace, pop: true, dedent: true },
        { regex: /:/, token: semiColon },
        { regex: /\|/, token: semiColon },
        { regex: /[^{}:\|]+/, token: 'string' }
    ],
    meta: {}
});
