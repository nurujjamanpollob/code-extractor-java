/**
 * Block comment for the module.
 */

import { helper } from './utils';

// Single line comment
const GLOBAL_CONST = 42;

/**
 * A class in JavaScript.
 */
class Comprehensive {
    constructor(name) {
        this.name = name;
    }

    /**
     * Async method with arrow function inside.
     */
    async processData(items) {
        // Arrow function
        const mapped = items.map(item => {
            console.log(`Processing ${item}`);
            return item * 2;
        });

        // Promise and async patterns
        return await Promise.all(mapped.map(async x => {
            const result = await helper(x);
            return result;
        }));
    }

    // Static method
    static create(name) {
        return new Comprehensive(name);
    }
}

/**
 * Top level function.
 */
function topLevelFunction(arg) {
    const inner = (x) => x + arg;
    return inner(10);
}

// Exporting
export default Comprehensive;
export { topLevelFunction };
