/**
 * SPDX-FileCopyrightText: 2015-2024 tracetronic GmbH <info@tracetronic.de>
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

buildPlugin(
  useContainerAgent: true, // Set to `false` if you need to use Docker for containerized tests
  configurations: [
    [platform: 'linux', jdk: 17],
    [platform: 'windows', jdk: 17],
    [platform: 'linux', jdk: 21],
    [platform: 'windows', jdk: 21]
  ],
  useArtifactCachingProxy: false // Use plugins.jenkins.io directly
)
