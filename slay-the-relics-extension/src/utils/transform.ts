// Transform utilities for coordinate transformation to support non-fullscreen layouts

export interface Transform {
  offsetX: number; // % offset from left
  offsetY: number; // % offset from top
  scaleX: number;  // % width scale (100 = full width)
  scaleY: number;  // % height scale (100 = full height)
}

// Default transform for fullscreen (no transformation)
export const DEFAULT_TRANSFORM: Transform = {
  offsetX: 0,
  offsetY: 0,
  scaleX: 100,
  scaleY: 100,
};

/**
 * Transform a single coordinate value
 * @param value - Original coordinate value (as percentage of game window)
 * @param offset - Stream offset (as percentage of stream canvas)
 * @param scale - Scale factor (100 = no scaling, 50 = half size)
 * @returns Transformed coordinate (as percentage of stream canvas)
 */
export function transformCoord(
  value: number,
  offset: number,
  scale: number,
): number {
  return offset + (value * scale) / 100;
}

/**
 * Transform X coordinate from game space to stream space
 */
export function transformX(x: number, transform: Transform): number {
  return transformCoord(x, transform.offsetX, transform.scaleX);
}

/**
 * Transform Y coordinate from game space to stream space
 */
export function transformY(y: number, transform: Transform): number {
  return transformCoord(y, transform.offsetY, transform.scaleY);
}

/**
 * Transform a hitbox from game space to stream space
 */
export interface HitBox {
  x: string | number;
  y: string | number;
  w: string | number;
  h: string | number;
  z: number | string;
}

export function transformHitBox(
  hitbox: { x: number; y: number; w: number; h: number; z: number | string },
  transform: Transform,
): HitBox {
  return {
    x: `${transformX(hitbox.x, transform)}%`,
    y: `${transformY(hitbox.y, transform)}%`,
    w: `${(hitbox.w * transform.scaleX) / 100}%`,
    h: `${(hitbox.h * transform.scaleY) / 100}%`,
    z: hitbox.z,
  };
}

/**
 * Transform a position string (e.g., "10%") to transformed value
 */
export function transformPercentageString(
  value: string,
  offset: number,
  scale: number,
): string {
  const numValue = parseFloat(value);
  if (isNaN(numValue)) {
    return value;
  }
  return `${transformCoord(numValue, offset, scale)}%`;
}
